
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
 * Creates a bright ring based on the waveform of the audio signal.
 * 
 * @author christopher
 */
public class Wavering extends VisualizerAbstract
{
	private static final long serialVersionUID = 7540082043727857258L;

	private boolean _colorAbsolute;
	private float _rx, _ry, _dx, _dy;

	/**
	 * Ctor.
	 */
	public Wavering()
	{
		colorAbsolute(true);
		setColor(0xFFFF0000, 0xFFFFFF00, PApplet.RGB);
		setRadius(250, 250);
		setDisplacement(100, 100);
	}
	
	@Override
	public void process() throws PAVException
	{
		p.strokeWeight(2);
		p.noFill();
		p.beginShape();
				
		float[] frame = Frame.samples();
		float[] area = getArea();
		int len = frame.length;
		float ox = (area[2] - area[0]) / 2;
		float oy = (area[3] - area[1]) / 2;
		
		float as = -(PApplet.PI * 1.5f);
		float ae = as + PApplet.TWO_PI;
		
		int fl = 30;
		int j = (- len) + fl;
		
		float diff = (frame[len - 1] - frame[0]) / 2;
		float ddiff = diff / fl;
		
		for(int i = 0; i < len; i++, j++) {
			float v = frame[i];
			
			if(i < fl) v += diff - i * ddiff;
			else if(j >= 0) v -= j * ddiff;
			
			float angle = PApplet.map(i, 0, len, as, ae);
			float x = ox + (_rx + v * _dx) * PApplet.cos(angle);
			float y = oy + (_ry + v * _dy) * PApplet.sin(angle);
			
			if(_colorAbsolute && v < 0)	p.stroke(cm.map(v * -1)); else p.stroke(cm.map(v));
						
			p.vertex(x, y);
		}
		
		p.endShape(PApplet.CLOSE);
	}

	@Override
	public String toString()
	{
		return "Wavering";
	}

	@Override
	public void dispose() { }
	
	/**
	 * Sets the base radius of the wavering.
	 * 
	 * @param rx Radius in x direction
	 * @param ry Radius in y direction
	 */
	public void setRadius(float rx, float ry)
	{
		_rx = rx;
		_ry = ry;
	}
	
	/**
	 * Sets the maximum displacement of the ring coordinates.
	 * 
	 * @param dx Maximum displacement in x direction
	 * @param dy Maximum displacement in y direction
	 */
	public void setDisplacement(float dx, float dy)
	{
		_dx = dx;
		_dy = dy;
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
}
