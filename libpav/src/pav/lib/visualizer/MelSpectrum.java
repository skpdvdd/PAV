
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
import pav.lib.Util;
import pav.lib.frame.Frame;
import pav.lib.frame.TransformResult;
import processing.core.PApplet;

/**
 * Draws a MEL spectrum of the currently playing audio data.
 * 
 * @author christopher
 */
public class MelSpectrum extends VisualizerAbstract
{
	private static final long serialVersionUID = 5375994108922066833L;

	private transient float _vMax;
	
	private int _borderColor, _numBands, _quantizationSteps;
	private float[] _filter;
	private boolean _rememberMax;
	
	/**
	 * Ctor.
	 */
	public MelSpectrum()
	{
		rememberMax(true);
		setNumBands(40);
		setColor(new float[] { 0, 0.2f, 0.6f, 1 }, new int[] { 0xFF0000FF, 0xFF00FF00, 0xFFFFFF00, 0xFFFF0000 }, PApplet.RGB);
	}
	
	@Override
	public void process() throws PAVException
	{
		TransformResult spectrum = Frame.Transform.melSpectrum(_numBands);
		float[] freq = spectrum.frame();
		float vMax = spectrum.max();
		
		if(_filter != null) {
			int len = _filter.length;
			freq = (float[]) freq.clone();	// changes to TransformResults are not allowed
			
			for(int i = 0; i < len; i++) {
				freq[i] = freq[i] * _filter[i];
				
				if(freq[i] > vMax) {
					vMax = freq[i];
				}
			}
		}
		
		if(! _rememberMax) {
			_vMax = 0;
		}
		
		boolean quantize = _quantizationSteps > 0;
		
		if(vMax > _vMax) {
			_vMax = vMax;
			
			if(quantize) {
				cm.setRange(1, _quantizationSteps);
			}
			else {
				cm.setRange(0, _vMax);
			}
		}
		
		if(_quantizationSteps > 0) {
			freq = Util.quantize(freq, _quantizationSteps, 0, _vMax);
		}
		
		int len = freq.length;
		float[] area = getArea();
		float tn = (area[2] - area[0]) / len;
		float min = (quantize) ? 1 : 0;
		float max = (quantize) ? _quantizationSteps : _vMax;		
		
		for(int i = 0; i < len; i++) {
			float v = freq[i];
			float y = PApplet.map(v, min, max, area[3], area[1]);
			float x = PApplet.map(i, 0, len - 1, area[0], area[2] - tn);
			p.stroke(_borderColor);
			p.fill(cm.map(v));
			p.rect(x, y, tn, area[3] - y);
		}
	}
	
	/**
	 * Sets the number of mel bands to compute. Must be > 0.
	 * 
	 * @param num The number of mel bands
	 */
	public void setNumBands(int num)
	{
		_numBands = num;
	}
	
	/**
	 * Sets the color to use when drawing the borders of the frequency bands.
	 * 
	 * @param color The color to use when drawing
	 */
	public void setBorderColor(int color)
	{
		_borderColor = color;
	}
	
	/**
	 * Sets the number of steps to use for quantization of the spectrum values or disables quantization.
	 * 
	 * @param steps The number of quantization steps. Set to <= 0 to disable quantization
	 */
	public void quantize(int steps)
	{
		_quantizationSteps = (steps < 0) ? 0 : steps;
		_vMax = 0;
	}
	
	/**
	 * Filters the calculated spectrum intensities before displaying them.
	 * The process is simply carried out by multiplying the i-th spectrum intensity
	 * with the i-th value in the filter. Thus, the filter length must not be longer
	 * than the number of calculated bands, but it can be lower.
	 * 
	 * @param filter The filter to use or null to disable filtering. Filter length must not be larger than numBands.
	 */
	public void filter(float[] filter)
	{
		_filter = filter;
		_vMax = 0;
	}
	
	/**
	 * Before drawing the maximum intensity of the spectrum data is calculated so that the output
	 * can be scaled properly. This method sets whether the maximum will be saved and reused.
	 * 
	 * @param remember Whether or not to remember the max intensity
	 */
	public void rememberMax(boolean remember)
	{
		_rememberMax = remember;
	}

	@Override
	public String toString()
	{
		return "MelSpectrum (" + _numBands + ")";
	}
	
	@Override
	public void dispose() { }
}
