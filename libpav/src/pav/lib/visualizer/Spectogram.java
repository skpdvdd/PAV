
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

package pav.lib.visualizer;

import pav.lib.PAVException;
import pav.lib.StreamingBuffer;
import pav.lib.frame.Frame;
import pav.lib.frame.TransformResult;
import processing.core.PApplet;

/**
 * A two dimensional spectogram.
 * 
 * @author christopher
 */
public class Spectogram extends VisualizerAbstract
{
	private static final long serialVersionUID = 3765068172167986718L;
	
	private transient float _vMax;
	private transient StreamingBuffer _buffer;
	
	private boolean _rememberMax, _highOnTop;
	private Integer _minFrequency, _maxFrequency;
	
	/**
	 * Ctor.
	 */
	public Spectogram()
	{
		rememberMax(true);
		noCutoffFrequencies();
		setHighOnTop(true);
		setColor(new float[] { 0, 0.1f, 0.5f, 1 }, new int[] { 0x00000000, 0xFF0000FF, 0xFF33FF33, 0xFFFF0000 }, PApplet.RGB);
	}
	
	@Override
	public void process() throws PAVException
	{
		float max;
		int from, to;
		
		TransformResult spectrum = Frame.Transform.spectrum();
		float[] bands = spectrum.frame();
		
		if(_minFrequency == null || _maxFrequency == null) {
			max = spectrum.max();
			from = 0;
			to = bands.length - 1;
		}
		else {
			max = 0;
			from = Frame.Transform.Util.frequencyToBand(_minFrequency);
			to = Frame.Transform.Util.frequencyToBand(_maxFrequency);
			
			float sMax = spectrum.max();
			
			for(int i = from; i <= to; i++) {
				float v = bands[i];
				
				if(v == sMax) {
					max = v;
					break;
				}
				
				if(v > max) {
					max = v;
				}
			}
		}
		
		int numBands = (to - from) + 1;
		
		if(_rememberMax) {
			if(_vMax > max) {
				max = _vMax;
			}
			else {
				_vMax = max;
			}
		}
		
		cm.setRange(0, max);
		
		float[] area = getArea();
		int width = (int) Math.floor(area[2] - area[0]);
		int height = (int) Math.floor(area[3] - area[1]);
		
		if(_buffer == null || width != _buffer.getWidth() || numBands != _buffer.getHeight()) {
			if(_buffer != null) {
				_buffer.dispose();
			}
			
			_buffer = new StreamingBuffer(p, width, numBands);
		}
		
		cm.setRange(0, _vMax);
		int[] colors = new int[numBands];
		
		for(int i = from; i <= to; i++) {
			colors[i] = cm.map(bands[i]);
		}
		
		_buffer.add(colors, _highOnTop);
		_buffer.draw((int) area[0], (int) area[1], width, height);
	}
			
	/**
	 * Before drawing the maximum intensity of the frequency data is calculated so that the output
	 * can be scaled properly. By default this information is stored and reused if the maximum of the
	 * current frame is lower. if set to false, a new maximum will be calculated for every new frame.
	 * 
	 * @param remember Whether or not to remember the max intensity
	 */
	public void rememberMax(boolean remember)
	{
		_rememberMax = remember;
	}
	
	/**
	 * Sets the cutoff frequencies. This will cut all frequency bands with lower
	 * frequencies, the first used band will be the one with the frequency min in it.
	 * The same counts for max. This means that the cutoff is not very precise.
	 * 
	 * @param min The minimum frequency. Must be >= 0
	 * @param max The maximum frequency. Must be <= 22050 and > min
	 */
	public void setCutoffFrequencies(int min, int max)
	{
		_minFrequency = min;
		_maxFrequency = max;
	}
	
	/**
	 * Tells the visualizer not to use cutoff frequencies. See cutoffFrequencies().
	 */
	public void noCutoffFrequencies()
	{
		_minFrequency = null;
		_maxFrequency = null; 
	}
	
	/**
	 * Whether to draw high frequencies at the top of the visualization or not.
	 * 
	 * @param onTop Whether to draw high frequencies on top.
	 */
	public void setHighOnTop(boolean onTop)
	{
		_highOnTop = onTop;
	}

	@Override
	public String toString()
	{
		return "Spectogram";
	}
}
