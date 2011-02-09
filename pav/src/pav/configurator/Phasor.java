
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

/**
 * Configurator for the Phasor visualizer.
 * 
 * @author christopher
 */
public class Phasor extends ConfiguratorAbstract
{
	@Override
	public boolean process(Visualizer subject, String query)
	{
		if(! (subject instanceof pav.lib.visualizer.Phasor)) {
			return false;
		}
		
		String[] q = query.split(" ");
		
		if(q.length < 2) {
			return false;
		}
		
		if(q[0].equals("mode")) {
			return _processMode((pav.lib.visualizer.Phasor) subject, Util.removeFirst(q));
		}
		
		return false;
	}
	
	private boolean _processMode(pav.lib.visualizer.Phasor subject, String[] query)
	{
		if(query[0].equals("dots")) {
			subject.setMode(pav.lib.visualizer.Phasor.MODE_DOTS);
		}
		else if(query[0].equals("lines")) {
			subject.setMode(pav.lib.visualizer.Phasor.MODE_LINES);
		}
		else if(query[0].equals("curves")) {
			subject.setMode(pav.lib.visualizer.Phasor.MODE_CURVES);
		}
		else {
			return false;
		}
		
		return true;
	}
}
