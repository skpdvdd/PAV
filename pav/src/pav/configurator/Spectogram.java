
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
 * Configurator for the Spectogram visualizer.
 * 
 * @author christopher
 */
public class Spectogram extends ConfiguratorAbstract
{
	@Override
	public boolean process(Visualizer subject, String query)
	{
		if(! (subject instanceof pav.lib.visualizer.Spectogram)) {
			return false;
		}
		
		String[] q = query.split(" ");
		
		if(q.length < 2) {
			return false;
		}
		
		if(q[0].equals("hightop")) {
			return _processHightop((pav.lib.visualizer.Spectogram) subject, Util.removeFirst(q));
		}
		
		if(q[0].equals("freq")) {
			return _processFreq((pav.lib.visualizer.Spectogram) subject, Util.removeFirst(q));
		}
		
		return false;
	}
	
	private boolean _processHightop(pav.lib.visualizer.Spectogram subject, String[] query)
	{
		subject.setHighOnTop(Util.parseBools(query)[0]);
		return true;
	}
	
	private boolean _processFreq(pav.lib.visualizer.Spectogram subject, String[] query)
	{
		int[] freqs = Util.tryParseInts(query);
		
		if(freqs.length == 1) {
			if(freqs[0] == 0) {
				subject.noCutoffFrequencies();
				return true;
			}
			else if(freqs[0] > 0) {
				subject.setCutoffFrequencies(0, freqs[0]);
				return true;
			}
		}
		else if(freqs.length == 2) {
			if(freqs[1] > freqs[0] && freqs[0] >= 0) {
				subject.setCutoffFrequencies(freqs[0], freqs[1]);
				return true;
			}
		}
		
		return false;
	}
}
