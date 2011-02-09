
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

package pav.configurator;

import pav.Util;
import pav.lib.visualizer.Visualizer;
import processing.core.PApplet;

/**
 * Configurator for all visualizers.
 * 
 * @author christopher
 */
public class Generic extends ConfiguratorAbstract
{
	@Override
	public boolean process(Visualizer subject, String query)
	{
		String[] q = query.split(" ");
		
		if(q.length < 2) {
			return false;
		}
		
		if(q[0].equals("color")) {
			return _processColor(subject, Util.removeFirst(q));
		}
		
		if(q[0].equals("area")) {
			return _processArea(subject, Util.removeFirst(q));
		}
		
		return false;
	}
	
	private boolean _processColor(Visualizer subject, String[] query)
	{
		int[] colors = Util.tryParseColors(query);
		
		if(colors.length == 0) return false;
		
		if(colors.length == 1) {
			subject.setColor(colors[0]);
		}
		else if(colors.length == 2) {
			subject.setColor(colors[0], colors[1], PApplet.RGB);
		}
		else {
			float[] thresholds = new float[colors.length];
			float delta = (float) (1.0 / (thresholds.length - 1));
			
			for(int i = 0; i < thresholds.length; i++) {
				thresholds[i] = i * delta;
			}
			
			subject.setColor(thresholds, colors, PApplet.RGB);
		}
		
		return true;
	}
	
	private boolean _processArea(Visualizer subject, String[] query)
	{
		float[] coords = Util.tryParseFloats(query);
		
		if(coords.length != 4) return false;
		
		for(float v : coords) {
			if(v < 0 || v > 1) return false;
		}
		
		if(coords[2] <= coords[0]) return false;
		if(coords[3] <= coords[1]) return false;
		
		subject.setArea(coords[0], coords[1], coords[2], coords[3], true);
		
		return true;
	}
}
