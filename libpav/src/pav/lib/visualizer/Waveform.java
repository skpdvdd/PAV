
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
import processing.core.PApplet;

/**
 * Draws a waveform of the currently playing audio data.
 * 
 * @author christopher
 */
public class Waveform extends VisualizerAbstract
{
	private static final long serialVersionUID = -9219616480317414260L;
	
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
	
	private int _mode;
	private float _strokeWeight;
	private boolean _colorAbsolute;
	
	/**
	 * Ctor.
	 */
	public Waveform()
	{
		setMode(MODE_BINS);
		colorAbsolute(true);
		setStrokeWeight(1);
		setColor(0xFFFF0000, 0xFFFFFF00, PApplet.RGB);
	}

	@Override
	public void process() throws PAVException
	{
		p.strokeWeight(_strokeWeight);
				
		float[] frame = Frame.samples();
		float[] area = getArea();
		int len = frame.length;
		
		if(_mode == MODE_SHAPE) {
			p.noFill();
			p.beginShape();
		}
		
		for(int i = 0; i < len; i++) {
			float v = frame[i];
			float x = PApplet.map(i, 0, len - 1, area[0], area[2]);
			float y = PApplet.map(v, -1, 1, area[1], area[3]);
			
			if(_colorAbsolute && v < 0) {
				p.stroke(cm.map(v * -1));
			}
			else {
				p.stroke(cm.map(v));
			}
									
			switch(_mode) {
				case MODE_BINS :
					p.line(x, (area[1] + area[3]) / 2, x, y);
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
	 * Sets the visualization mode. Must be a valid mode according
	 * to the MODE_ constants of this class.
	 * 
	 * @param mode The visualization mode
	 */
	public void setMode(int mode)
	{
		_mode = mode;
	}
	
	/**
	 * Whether to use the absolute values for coloring, i.e. whether -1 gets the same color as 1 or not.
	 * 
	 * @param absolute Whether to use absolute coloring
	 */
	public void colorAbsolute(boolean absolute)
	{
		if(absolute) {
			_colorAbsolute = true;
			cm.setRange(0, 1);
		}
		else {
			_colorAbsolute = false;
			cm.setRange(-1, 1);
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
	
	@Override
	public String toString()
	{
		switch(_mode) {
			case MODE_BINS :
				return "Waveform (bin mode)";
			case MODE_DOTS :
				return "Waveform (dot mode)";
			case MODE_SHAPE :
				return "Waveform (shape mode)";
			default :
				return "Waveform";
		}
	}

	@Override
	public void dispose() { }
}
