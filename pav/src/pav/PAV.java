
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
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import pav.audiosource.AudioCallback;
import pav.audiosource.AudioSource;
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
public class PAV extends PApplet implements AudioCallback
{
	private static final int _frameDropUpdateInterval = 200;
	
	private static final long serialVersionUID = 1525235544995508743L;
	
	private StringBuilder _inputBuffer;
	private Visualization _visualization;
	private final AudioSource _audioSource;
	private volatile String[] _audioSourceInfo;
	
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
	 * @throws PAVException If an error occured while initializing the audio source
	 */
	public PAV() throws PAVException
	{
		_drawStatus = true;
		_inputBuffer = new StringBuilder();
		_inputHistory = new ArrayList<String>();
		_configurators = new ArrayList<Configurator>();
		_configurators.add(ConfiguratorFactory.generic());
		_sampleQueue = new LinkedBlockingDeque<float[]>();
		_audioSourceInfo = new String[0];
		_audioSource = AudioSource.factory(this);
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
		
		_audioSource.read();
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
			System.out.println("Interrupted while waiting for new data ... aborting.");
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

		try {
			_audioSource.close();
		}
		catch(InterruptedException e) { }
		
		super.exit();
	}
	
	@Override
	public void keyPressed()
	{
		if(_inputBuffer.length() == 0 && key == 's') {
			_drawStatus = !_drawStatus;
			return;
		}
		
		if(_inputBuffer.length() == 0 && key == 'p') {
			saveFrame("pav-####.png");
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
				if(_inputHistoryPosition == _inputHistory.size() - 1) {
					_inputHistoryPosition = 0;
					_inputBuffer = new StringBuilder();
				}
				else {
					_inputHistoryPosition++;
					_inputBuffer = new StringBuilder(_inputHistory.get(_inputHistoryPosition));
				}		
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
	
	@Override
	public void onNewFrame(float[] frame)
	{
		_sampleQueue.add(frame);
	}

	@Override
	public void onSongChanged()
	{
		//TODO implement
	}
	
	@Override
	public void onSampleRateChanged(int sampleRate)
	{
		_visualization.setSampleRate(sampleRate);
	}

	@Override
	public void onStatusChanged(String[] info)
	{
		_audioSourceInfo = info;
	}

	@Override
	public void onError(Throwable error)
	{
		_audioSourceInfo = new String[] { "AudioSource Error: " + error.getMessage() };
		Console.error(error);
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
		
		fill(0xFFFF0000);
		
		for(String s : _audioSourceInfo) {
			text(s, x, y);
			y += 20;
		}
		
		fill(0xFF00FF00);
		text("Type in this window to execute commands.", x, y);
		y += 20;
		
		fill(255);
		text("Type 's' to toggle this information.", x, y);
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
}
