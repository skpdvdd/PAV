
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
import pav.lib.frame.Frame;
import pav.lib.frame.TransformResult;
import processing.core.PApplet;

/**
 * Draws the logarithm of the frequency spectrum of the currently playing audio data.
 * 
 * @author christopher
 */
public class Spectrum extends VisualizerAbstract
{
	private static final long serialVersionUID = -7118841157652718274L;
	
	/**
	 * Draw lines.
	 */
	public static final int MODE_BINS = 1;
	
	/**
	 * Draw dots.
	 */
	public static final int MODE_DOTS = 2;
	
	/**
	 * Draw a shape.
	 */
	public static final int MODE_SHAPE = 3;
	
	private transient float _vMax;
	
	private int _mode;
	private float _strokeWeight;
	private boolean _rememberMax;
	private Integer _minFrequency, _maxFrequency;
	
	/**
	 * Ctor.
	 */
	public Spectrum()
	{
		rememberMax(true);
		setMode(MODE_BINS);
		setStrokeWeight(1);
		noCutoffFrequencies();
		setColor(new float[] { 0, 0.2f, 0.6f, 1 }, new int[] { 0xFF0000FF, 0xFF00FF00, 0xFFFFFF00, 0xFFFF0000 }, PApplet.RGB);
	}
	
	@Override
	public void process() throws PAVException
	{	
		p.strokeWeight(_strokeWeight);
		
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
		
		if(_mode == MODE_SHAPE) {
			p.noFill();
			p.beginShape();
		}
		
		for(int i = from; i <= to; i++) {
			float v = bands[i];
			float x = PApplet.map(i, from, to, area[0], area[2]);
			float y = PApplet.map(v, 0, max, area[3], area[1]);
			
			p.stroke(cm.map(v));
			
			switch(_mode) {
				case MODE_BINS :
					p.line(x, area[3], x, y);
					break;
				case MODE_DOTS :
					p.point(x, y);
					break;
				case MODE_SHAPE :	
					p.vertex(x, y);	
					break;
			}
		}
		
		if(_mode == MODE_SHAPE) {
			p.endShape();
		}
	}
	
	/**
	 * Sets the stroke weight to use when drawing.
	 * 
	 * @param weight The stroke weight. Must be > 0
	 */
	public void setStrokeWeight(float weight)
	{
		_strokeWeight = weight;
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
	 * Sets the visualization mode. Must be a valid mode according
	 * to the MODE_ constants of this class.
	 * 
	 * @param mode
	 */
	public void setMode(int mode)
	{
		_mode = mode;
	}
		
	/**
	 * Before drawing the maximum intensity of the frequency data is calculated so that the output
	 * can be scaled properly. This method sets whether the maximum will be saved and reused.
	 * 
	 * @param remember Whether to remember the max intensity
	 */
	public void rememberMax(boolean remember)
	{
		_rememberMax = remember;
	}
		
	@Override
	public String toString()
	{
		switch(_mode) {
			case MODE_BINS :
				return "Spectrum (bin mode)";
			case MODE_DOTS :
				return "Spectrum (dot mode)";
			case MODE_SHAPE :
				return "Spectrum (shape mode)";
			default :
				return "Spectrum";
		}
	}
	
	@Override
	public void dispose() { }
}
