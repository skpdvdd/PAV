
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
 * Configurator for the Spectrum visualizer.
 * 
 * @author christopher
 */
public class Spectrum extends ConfiguratorAbstract
{	
	@Override
	public boolean process(Visualizer subject, String query)
	{
		if(! (subject instanceof pav.lib.visualizer.Spectrum)) {
			return false;
		}
		
		String[] q = query.split(" ");
		
		if(q.length < 2) {
			return false;
		}
		
		if(q[0].equals("mode")) {
			return _processMode((pav.lib.visualizer.Spectrum) subject, Util.removeFirst(q));
		}
		
		if(q[0].equals("freq")) {
			return _processFreq((pav.lib.visualizer.Spectrum) subject, Util.removeFirst(q));
		}
		
		if(q[0].equals("sw")) {
			return _processStrokeWeight((pav.lib.visualizer.Spectrum) subject, Util.removeFirst(q));
		}
		
		return false;
	}
	
	private boolean _processMode(pav.lib.visualizer.Spectrum subject, String[] query)
	{
		if(query[0].equals("bins")) {
			subject.setMode(pav.lib.visualizer.Waveform.MODE_BINS);
			return true;
		}
		else if(query[0].equals("dots")) {
			subject.setMode(pav.lib.visualizer.Waveform.MODE_DOTS);
			return true;
		}
		else if(query[0].equals("shape")) {
			subject.setMode(pav.lib.visualizer.Waveform.MODE_SHAPE);
			return true;
		}
		
		return false;
	}
	
	private boolean _processFreq(pav.lib.visualizer.Spectrum subject, String[] query)
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
	
	private boolean _processStrokeWeight(pav.lib.visualizer.Spectrum subject, String[] query)
	{
		try {
			float sw = Float.parseFloat(query[0]);
			if(sw > 0) {
				subject.setStrokeWeight(sw);
				return true;
			}
			
			return false;
		}
		catch(NumberFormatException e) {
			return false;
		}
	}
}
