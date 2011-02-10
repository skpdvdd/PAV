
/*
 * Processing Audio Visualization (PAV)
 * Copyright (C) 2011  Christopher Pramerdorfer
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package pav;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import pav.configurator.Configurator;
import pav.configurator.ConfiguratorFactory;
import pav.lib.PAVException;
import pav.lib.ShaderManager;
import pav.lib.Visualization;
import pav.lib.VisualizationImpl;
import pav.lib.visualizer.Boxes;
import pav.lib.visualizer.Bubbles;
import pav.lib.visualizer.MelSpectrum;
import pav.lib.visualizer.Phasor;
import pav.lib.visualizer.Rainbow;
import pav.lib.visualizer.Spectogram;
import pav.lib.visualizer.Spectrum;
import pav.lib.visualizer.Visualizer;
import pav.lib.visualizer.Waveform;
import processing.core.PApplet;
import processing.core.PFont;
import codeanticode.glgraphics.GLGraphics;

/**
 * Processing Audio Visualization.
 * 
 * @author christopher
 */
public class PAV extends PApplet
{
	private static final int _frameDropUpdateInterval = 200;
	
	private static final long serialVersionUID = 1525235544995508743L;
	
	private final Thread _clientConnectionThread;
	private final ClientConnection _clientConnection;
	private StringBuilder _inputBuffer;
	private Visualization _visualization;
	
	private PFont _statusFont;
	private boolean _drawStatus;
	private int _inputHistoryPosition;
	private final ArrayList<String> _inputHistory;
	private final ArrayList<Configurator> _configurators;
	private final BlockingDeque<float[]> _sampleQueue;
	
	private float _frameDropPercentage;
	private int _numFramesVisualized;
	private volatile int _numFramesReceived;
	
	/**
	 * Ctor.
	 * 
	 * @throws IOException If an error occured while initializing the ControlStream.
	 */
	public PAV() throws IOException
	{
		_drawStatus = true;
		_inputBuffer = new StringBuilder();
		_inputHistory = new ArrayList<String>();
		_configurators = new ArrayList<Configurator>();
		_configurators.add(ConfiguratorFactory.generic());
		_sampleQueue = new LinkedBlockingDeque<float[]>();
		
		_clientConnection = new ClientConnection(Config.port);
		_clientConnectionThread = new Thread(_clientConnection, "ClientConnection");
		_clientConnectionThread.start();
	}

	/**
	 * Internal method called by Processing. Not to be called from outside.
	 */
	@Override
	public void setup()
	{
		size(Config.windowWidth, Config.windowHeight, Config.renderer);
		background(0);
		frameRate(100);

		_statusFont = createFont("sans", 12, true);
		textFont(_statusFont);
		textSize(12);

		frame.setResizable(Config.windowResizable);
		frame.setTitle("PAV");

		WindowListener[] listeners = frame.getWindowListeners();

		for(int i = 0; i < listeners.length; i++) {
			frame.removeWindowListener(listeners[i]);
		}

		frame.addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(WindowEvent e)
			{
				exit();
			}
		});
		
		_visualization = new VisualizationImpl(this);
		_visualization.setSampleRate(44100);
		
		if(g instanceof GLGraphics) {
			try {
				ShaderManager.initialize(this);
			}
			catch(PAVException e) { }
		}
	}

	/**
	 * Internal method called by Processing. Not to be called from outside.
	 */
	@Override
	public void draw()
	{
		background(0);
	
		try {
			float[] frame = _sampleQueue.pollLast(66, TimeUnit.MILLISECONDS);
			
			if(frame != null) {
				int len = _sampleQueue.size();
				
				if(len > 0) {
					_sampleQueue.clear();
				}
				
				_numFramesReceived += len + 1;
				
				_numFramesVisualized++;
				_visualization.process(frame);	
			}
		}
		catch(InterruptedException e) {
			System.out.println("ir");
			return;
		}
		catch (PAVException e) {
			Console.error("An error occured while drawing the visualization:");
			Console.error(e.getMessage());
			exit();
		}
		
		if(frameCount % _frameDropUpdateInterval == 0) {
			_frameDropPercentage = (_numFramesReceived - _numFramesVisualized) / 2f;
			_numFramesVisualized = 0;
			_numFramesReceived = 0;
		}

		_drawInput();
		
		if(_drawStatus) {
			_drawStatus();
		}
	}

	/**
	 * Closes the program.
	 */
	@Override
	public void exit()
	{
		Console.out("Shutting down ...");

		_clientConnectionThread.interrupt();
		_clientConnection.close();

		try {
			_clientConnectionThread.join(500);
		}
		catch (InterruptedException e) { }

		super.exit();
	}
	
	private void _drawInput()
	{
		if(_inputBuffer.length() == 0) {
			return;
		}
		
		textAlign(RIGHT, BOTTOM);
		fill(0xFF00FF00);
		text("Input: '" + _inputBuffer.toString() + "'", width - 10, height - 10);
	}
	
	private void _drawStatus()
	{
		textAlign(RIGHT, TOP);
		
		int x = width - 10;
		int y = 10;
		
		fill(0xFFAAAA00);
		text("Frames dropped: " + _frameDropPercentage + "%", x, y);
		y += 20;
				
		if(_visualization.numVisualizers() == 0) {
			fill(0xFFFF0000);
			text("No visualizers active.", x, y);
			y += 20;
		}
		else {
			fill(200);
			
			for(Map.Entry<Integer, Visualizer> v : _visualization.getVisualizers().entrySet()) {
				text("[" + v.getKey() + "] - " + v.getValue(), x, y);
				y += 18;
			}
		}
		
		if(! _clientConnection.isConnected()) {
			fill(0xFFFF0000);
			text("No client connection.", x ,y);
			y += 20;
		}
		
		fill(0xFF00FF00);
		text("Type in this window to execute commands.", x, y);
		y += 20;
		
		fill(255);
		text("Type 's' to toggle this information.", x, y);
	}
	
	@Override
	public void keyPressed()
	{
		if(_inputBuffer.length() == 0 && key == 's') {
			_drawStatus = !_drawStatus;
			return;
		}
		
		if(keyCode == 38) {
			if(! _inputHistory.isEmpty()) {
				_inputHistoryPosition = (_inputHistoryPosition == 0) ? _inputHistory.size() - 1 : _inputHistoryPosition - 1;
				_inputBuffer = new StringBuilder(_inputHistory.get(_inputHistoryPosition));
			}
			
			return;
		}
		
		if(keyCode == 40) {
			if(! _inputHistory.isEmpty()) {
				_inputHistoryPosition++;
				
				if(_inputHistoryPosition > _inputHistory.size() - 1) {
					_inputHistoryPosition = 0;
				}
				
				_inputBuffer = new StringBuilder(_inputHistory.get(_inputHistoryPosition));
			}
			
			return;
		}
		
		if(keyCode == 8) {
			if(_inputBuffer.length() > 0) {
				_inputBuffer.deleteCharAt(_inputBuffer.length() - 1);
			}
	
			return;
		}
		
		if(keyCode != 10) {
			_inputBuffer.append(key);
			return;
		}
		
		boolean valid = false;
		String[] in = _inputBuffer.toString().split(" ");

		if(in[0].equals("add") && in.length >= 2) {
			valid = _addVisualizer(Util.removeFirst(in));
		}
		else if(in[0].equals("rem") && in.length == 2) {
			valid = _removeVisualizer(in[1]);
		}
		else if(in[0].equals("c") && _inputBuffer.length() > 2) {
			valid = _configureVisualizer(_inputBuffer.substring(2, _inputBuffer.length()));
		}
		
		if(valid) {
			_inputHistory.add(_inputBuffer.toString());
			_inputHistoryPosition = 0;
		}
		
		_inputBuffer = new StringBuilder();
	}
	
	private boolean _addVisualizer(String[] in)
	{
		String name = in[0];
		Integer level = null;
		
		if(in.length == 2) {
			try {
				level = new Integer(Integer.parseInt(in[1]));
			}
			catch(NumberFormatException e) { }
		}
		
		try {
			Configurator configurator = null;
			
			if(name.equals("waveform")) {
				_addVisualizer(new Waveform(), level);
				configurator = ConfiguratorFactory.waveform();
			}
			else if(name.equals("boxes")) {
				if(! (g instanceof GLGraphics)) {
					System.out.println("Visualizer requires GLGraphics mode.");
					return false;
				}
				
				_addVisualizer(new Boxes(), level);
				configurator = ConfiguratorFactory.boxes();
			}
			else if(name.equals("bubbles")) {
				if(! (g instanceof GLGraphics)) {
					System.out.println("Visualizer requires GLGraphics mode.");
					return false;
				}
				
				_addVisualizer(new Bubbles(), level);
				configurator = ConfiguratorFactory.bubbles();
			}
			else if(name.equals("phasor")) {
				_addVisualizer(new Phasor(), level);
				configurator = ConfiguratorFactory.phasor();
			}
			else if(name.equals("rainbow")) {				
				_addVisualizer(new Rainbow(), level);
				configurator = ConfiguratorFactory.rainbow();
			}
			else if(name.equals("spectogram")) {
				_addVisualizer(new Spectogram(), level);
				configurator = ConfiguratorFactory.spectogram();
			}
			else if(name.equals("spectrum")) {
				_addVisualizer(new Spectrum(), level);
				configurator = ConfiguratorFactory.spectrum();
			}
			else if(name.equals("melspectrum")) {
				_addVisualizer(new MelSpectrum(), level);
				configurator = ConfiguratorFactory.melSpectrum();
			}
			else {
				return false;
			}
			
			if(configurator != null && ! (_configurators.contains(configurator))) {
				_configurators.add(configurator);
			}
			
			return true;
		}
		catch (PAVException e) {
			Console.error("An error occured while adding a visualizer:");
			Console.error(e);
			return false;
		}
	}
	
	private void _addVisualizer(Visualizer v, Integer level) throws PAVException
	{
		if(level == null) {
			_visualization.addVisualizer(v);
		}
		else {
			_visualization.addVisualizer(v, level);
		}
	}
	
	private boolean _removeVisualizer(String level)
	{
		try {
			_visualization.removeVisualizerAt(Integer.parseInt(level));
			return true;
		}
		catch (NumberFormatException e)
		{
			return false;
		}
	}
	
	private boolean _configureVisualizer(String query)
	{
		String[] q = query.split(" ");
		
		if(q.length < 3) return false;
		
		Visualizer subject = null;
		
		try {
			subject = _visualization.getVisualizer(Integer.parseInt(q[0]));
		}
		catch(NumberFormatException e) {
			return false;
		}

		if(subject == null) return false;
		
		query = query.substring(q[0].length() + 1);

		for(Configurator c : _configurators) {
			if(c.process(subject, query) == true) {
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Handles connections with clients.
	 * 
	 * @author christopher
	 */
	private class ClientConnection implements Runnable
	{
		private final ServerSocket _serverSocket;

		private Socket _socket;
		private DataStream _dataStream;
		private Thread _dataStreamThread;
		private PrintWriter _out;
		private BufferedReader _in;
		private volatile boolean _done;
		private volatile boolean _isConnected;

		/**
		 * Ctor.
		 * 
		 * @param port The port to listen to for new connections
		 * @throws IOException If an error occured while setting up the ServerSocket
		 */
		public ClientConnection(int port) throws IOException
		{
			_serverSocket = new ServerSocket(port, 0);
		}
		
		/**
		 * Returns true if a client connection is established, otherwise false.
		 * 
		 * @return True if there is a client connection
		 */
		public boolean isConnected()
		{
			return _isConnected;
		}

		@Override
		public void run()
		{
			while(! _done) {
				try {
					_socket = _serverSocket.accept();
					_out = new PrintWriter(_socket.getOutputStream(), true);
					_in = new BufferedReader(new InputStreamReader(_socket.getInputStream()));

					_dataStream = new DataStream();
					_dataStreamThread = new Thread(_dataStream, "DataStream");
					_dataStreamThread.start();
					_out.println("!ok " + _dataStream.getLocalPort());

					_isConnected = true;
					String line = null;

					while((line = _in.readLine()) != null) {
						String[] in = line.split(" ");

						if(in[0].equals("!close")) {
							break;
						}

						if(in[0].equals("!sr")) {
							try {
								_visualization.setSampleRate(Integer.parseInt(in[1]));
							}
							catch (Exception e) {
								_out.println("!err");
							}
						}
					}
				}
				catch (IOException e) {
					if(! _done) {
						Console.out("An error occured while communicating with the client ... closing connection.");
					}
				}
				finally {
					_isConnected = false;
					_closeAll();
				}
			}

			try {
				_serverSocket.close();
			}
			catch (IOException e) { }
		}

		/**
		 * Closes the connection.
		 */
		public void close()
		{
			_done = true;
			_closeAll();
		}

		private void _closeAll()
		{
			if(_dataStream != null) {
				_dataStream.close();
			}

			if(_out != null) {
				_out.println("!close");
				_out.close();
			}

			if(_in != null) try { _in.close(); } catch (IOException e) { }
			if(_socket != null) try { _socket.close(); } catch (IOException e) { }
		}
		
		/**
		 * Represents a data stream from the client.
		 * 
		 * @author christopher
		 */
		private class DataStream implements Runnable
		{
			private Socket _socket;
			private volatile boolean _done;
			private final ServerSocket _serverSocket;

			/**
			 * Ctor.
			 * 
			 * Creates a new ServerSocket that listens to any free port.
			 * 
			 * @throws IOException If an error occured while initializing the ServerSocket
			 */
			public DataStream() throws IOException
			{
				_serverSocket = new ServerSocket(0, 0);
			}

			/**
			 * Returns the port the ServerSocket listens to.
			 * 
			 * @return The port
			 */
			public int getLocalPort()
			{
				return _serverSocket.getLocalPort();
			}

			@Override
			public void run()
			{
				DataInputStream in = null;
				FloatBuffer frameBuffer = null;
				byte[] byteIn = new byte[0];
				float[] frame = new float[0];
				
				try {
					_socket = _serverSocket.accept();
					in = new DataInputStream(_socket.getInputStream());

					while(! _done) {
						int frameLen = in.readInt();
						int frameLenBytes = frameLen * 4;
						
						if(byteIn.length != frameLenBytes) {
							byteIn = new byte[frameLenBytes];
							frameBuffer = ByteBuffer.wrap(byteIn).asFloatBuffer();
							frame = new float[frameLen];
						}
						
						in.readFully(byteIn);
						
						frameBuffer.clear();
						frameBuffer.get(frame, 0, frameLen);
						
						_sampleQueue.addLast(frame);
					}
				}
				catch (Exception e) {
					if(! _done) {
						Console.error("Error while reading frame data from the client ... closing connection.");
					}
				}
				finally {
					if(in != null) {
						try {
							in.close();
						}
						catch(IOException e) { }
					}
					
					_closeAll();
				}
			}

			/**
			 * Closes the connection.
			 */
			public void close()
			{
				_done = true;
				_closeAll();
			}
			
			private void _closeAll()
			{
				try { _serverSocket.close(); } catch (IOException e) { }
				if(_socket != null) try { _socket.close(); } catch (IOException e) { }
			}
		}
	}
}
