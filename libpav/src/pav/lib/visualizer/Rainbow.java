
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
import processing.core.PApplet;

/**
 * Draws colored stripes based on the frequency or the intenstiy of the playing music.
 * In frequency mode (MODE_FREQUENCY) the colors are chosen based on the frequency distribution
 * of the sound (spectral centroid). In intensity mode (MODE_INTENSITY) the colors are based
 * on the sound intensity (rms).
 * 
 * @author christopher
 */
public class Rainbow extends VisualizerAbstract
{
	/**
	 * Choose colors based on the frequency distribution.
	 */
	public static final int MODE_FREQUENCY = 1;
	
	/**
	 * Choose colors based on the sound intensity.
	 */
	public static final int MODE_INTENSITY = 2;
	
	private static final long serialVersionUID = 3758005144794379244L;

	private transient StreamingBuffer _buffer;
	
	private int _mode;
	private boolean _auto;
	private float _vMin, _vMax;
		
	/**
	 * Ctor.
	 */
	public Rainbow()
	{
		float[] thresholds = { 0, 0.143f, 0.286f, 0.429f, 0.572f, 0.702f, 0.845f, 1 };
		int[] colors = { 0xFF000000, 0xFFFF0000, 0xFFFFFF00, 0xFF00FF00, 0xFF00FFFF, 0xFF0000FF, 0xFF8800FF, 0xFFAA00FF };
		
		setMode(MODE_FREQUENCY);
		cm.setColor(thresholds, colors, PApplet.RGB);
	}
	
	@Override
	public void process() throws PAVException
	{
		float v = 0;
		
		if(_mode == MODE_FREQUENCY) {
			v = Frame.Descriptor.spectralCentroid();
		}
		else if(_mode == MODE_INTENSITY) {
			v = Frame.Descriptor.rms();
		}

		float[] area = getArea();
		int width = (int) (area[2] - area[0]);
		int height = (int) (area[3] - area[1]);

		if(_buffer == null || width != _buffer.getWidth()) {
			if(_buffer != null) {
				_buffer.dispose();
			}
			
			_buffer = new StreamingBuffer(p, width, 1);
		}
		
		if(_auto) {
			if(v < _vMin) {
				_vMin = v;
				cm.setRange(_vMin, _vMax);
			}
			if(v > _vMax) {
				_vMax = v;
				cm.setRange(_vMin, _vMax);
			}
		}
		else {
			if(v < _vMin) v = _vMin;
			if(v > _vMax) v = _vMax;
		}
		
		int c = cm.map(v);
		
		_buffer.add(new int[] { c });
		_buffer.draw((int) area[0], (int) area[1], width, height);
	}
	
	/**
	 * Sets the range to map colors to. For instance, if set to 500, 5000 (the default) the color
	 * mapping will be done based on values between these values, any values lower than 500 will
	 * be mapped to 500, and higher than 5000 to 5000. In intensity mode values are between 0 and 1.
	 * 
	 * @param min The min value to use for color mapping. Must be >= 0
	 * @param max The max value to use for color mapping. Must be > min
	 */
	public void setRange(float min, float max)
	{
		_vMin = min;
		_vMax = max;
		_auto = false;
		
		cm.setRange(min, max);
	}
	
	/**
	 * Tells the visualizer not to use a static range (see setRange()),
	 * but to calculate the range automatically.
	 */
	public void setAutoRange()
	{
		_auto = true;
		_vMin = 0;
		_vMax = 0;
	}
	
	/**
	 * Sets the color mode. Must be a valid mode according
	 * to the MODE_ constants of this class. This will automatically set the range
	 * to values that make sense for the chosen mode (see setRange()).
	 * 
	 * @param mode The mode to use. Must be valid (see MODE_ constants)
	 */
	public void setMode(int mode)
	{
		_mode = mode;
		
		if(_mode == MODE_FREQUENCY) {
			setRange(500, 5000);
		}
		else if(_mode == MODE_INTENSITY) {
			setAutoRange();
		}
	}

	@Override
	public String toString()
	{
		switch(_mode) {
			case MODE_FREQUENCY :
				return "Rainbow (frequency mode)";
			case MODE_INTENSITY :
				return "Rainbow (intensity mode)";
			default :
				return "Rainbow";
		}
	}
	
	@Override
	public void dispose()
	{
		if(_buffer != null) _buffer.dispose();
	}
}
