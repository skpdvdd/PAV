package pav;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import codeanticode.glgraphics.GLGraphics;
import pav.lib.PAVException;
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

/**
 * Processing Audio Visualization.
 * 
 * @author christopher
 */
public class PAV extends PApplet
{
	private static final long serialVersionUID = 1525235544995508743L;
	
	private final Thread _clientConnectionThread;
	private final ClientConnection _clientConnection;
	private StringBuilder _inputBuffer;
	private Visualization _visualization;
	
	private boolean _drawStatus;
	private final PFont _statusFont;
	private volatile float[] _frame;

	/**
	 * Ctor.
	 * 
	 * @throws IOException If an error occured while initializing the ControlStream.
	 */
	public PAV() throws IOException
	{
		_drawStatus = true;
		_frame = new float[1024];
		_statusFont = createFont("sans", 12, true);
		_inputBuffer = new StringBuilder();
		
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
		noLoop();

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
			Console.out("Adding Bubbles visualizer by default because it currently does not support adding at runtime.");
			
			try {
				_visualization.addVisualizer(new Bubbles());
			}
			catch (PAVException e) {
				Console.error("An error occured while adding Bubbles:");
				Console.error(e);
			}
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
			_visualization.process(_frame);
		}
		catch (PAVException e) {
			Console.error("An error occured while drawing the visualization:");
			Console.error(e.getMessage());
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
		textFont(_statusFont);
		textSize(12);
		fill(0xFF00FF00);
		text("Input: '" + _inputBuffer.toString() + "'", width - 10, height - 10);
	}
	
	private void _drawStatus()
	{
		textAlign(RIGHT, TOP);
		textFont(_statusFont);
		textSize(12);
		
		int x = width - 10;
		int y = 10;
				
		if(_visualization.numVisualizers() == 0) {
			fill(0xFFFF0000);
			text("No visualizers active. Add one by typing 'add x' and hit enter", x, y);
			y += 16;
			text("where x is {waveform, boxes, phasor, rainbow, spectogram, spectrum, melspectrum}.", x, y);
			y += 20;
		}
		else {
			fill(200);
			
			for(Map.Entry<Integer, Visualizer> v : _visualization.getVisualizers().entrySet()) {
				text(v.getKey() + " - " + v.getValue(), x, y);
				y += 18;
			}
		}
		
		if(! _clientConnection.isConnected()) {
			fill(0xFFFF0000);
			text("No client connection.", x ,y);
			y += 20;
		}
		
		fill(255);
		text("Type 's' to toggle this information.", x, y);
	}
	
	@Override
	public void keyPressed()
	{
		if(_inputBuffer.length() == 0 && key == 's') {
			_drawStatus = !_drawStatus;
			redraw();
			return;
		}
		
		if(keyCode != 10) {
			_inputBuffer.append(key);
			redraw();
			return;
		}
		
		String[] in = _inputBuffer.toString().split(" ");
		_inputBuffer = new StringBuilder();
		
		if(in[0].equals("add") && in.length == 2) {
			_addVisualizer(in[1]);
		}
		else if(in[0].equals("rem") && in.length == 2) {
			_removeVisualizer(in[1]);
		}
		
		redraw();
	}
	
	private void _addVisualizer(String name)
	{
		try {
			if(name.equals("waveform")) {
				_visualization.addVisualizer(new Waveform());
			}
			else if(name.equals("boxes")) {
				_visualization.addVisualizer(new Boxes());
			}
//			else if(name.equals("bubbles")) {
//				_visualization.addVisualizer(new Bubbles());
//			}
			else if(name.equals("phasor")) {
				_visualization.addVisualizer(new Phasor());
			}
			else if(name.equals("rainbow")) {
				_visualization.addVisualizer(new Rainbow());
			}
			else if(name.equals("spectogram")) {
				_visualization.addVisualizer(new Spectogram());
			}
			else if(name.equals("spectrum")) {
				_visualization.addVisualizer(new Spectrum());
			}
			else if(name.equals("melspectrum")) {
				_visualization.addVisualizer(new MelSpectrum());
			};
		}
		catch (PAVException e) {
			Console.error("An error occured while adding a visualizer:");
			Console.error(e);
		}
	}
	
	private void _removeVisualizer(String level)
	{
		try {
			_visualization.removeVisualizerAt(Integer.parseInt(level));
		}
		catch (NumberFormatException e) { }
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
		 * Returns true if a clien connection is established, otherwise false.
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
			while(! Thread.interrupted() && ! _serverSocket.isClosed()) {
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
					Console.out("An error occured while communicating with the client ... closing connection.");
				}
				finally {
					_isConnected = false;
					_closeAll();
				}
			}

			try {
				_serverSocket.close();
			}
			catch (IOException e) {
			}
		}

		/**
		 * Closes the connection.
		 */
		public void close()
		{
			try {
				_serverSocket.close();
				if(_socket != null) _socket.close();
			}
			catch (IOException e) { }
		}

		private void _closeAll()
		{
			if(_dataStream != null) _dataStream.close();

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
				ObjectInputStream in = null;

				try {
					_socket = _serverSocket.accept();
					in = new ObjectInputStream(_socket.getInputStream());

					while(true) {
						_frame = (float[]) in.readObject();
						redraw();
					}
				}
				catch (Exception e) {
					Console.error("Error while reading frame data from the client ... closing connection.");
				}
				finally {
					if(in != null) try { in.close(); } catch(IOException e) { }
					_closeAll();
				}
			}

			/**
			 * Closes the connection.
			 */
			public void close()
			{
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
