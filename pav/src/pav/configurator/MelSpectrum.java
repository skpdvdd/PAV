
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
 * Configurator for the MelSpectrum visualizer.
 * 
 * @author christopher
 */
public class MelSpectrum extends ConfiguratorAbstract
{
	@Override
	public boolean process(Visualizer subject, String query)
	{
		if(! (subject instanceof pav.lib.visualizer.MelSpectrum)) {
			return false;
		}
		
		String[] q = query.split(" ");
		
		if(q.length < 2) {
			return false;
		}
		
		if(q[0].equals("num")) {
			return _processNum((pav.lib.visualizer.MelSpectrum) subject, Util.removeFirst(q));
		}
		
		if(q[0].equals("bcolor")) {
			return _processBcolor((pav.lib.visualizer.MelSpectrum) subject, Util.removeFirst(q));
		}
		
		if(q[0].equals("quantize")) {
			return _processQuantize((pav.lib.visualizer.MelSpectrum) subject, Util.removeFirst(q));
		}
		
		return false;
	}
	
	private boolean _processNum(pav.lib.visualizer.MelSpectrum subject, String[] query)
	{
		int[] num = Util.tryParseInts(query);
		
		if(num.length != 1 || num[0] <= 0) return false;
		
		subject.setNumBands(num[0]);
		return true;
	}
	
	private boolean _processBcolor(pav.lib.visualizer.MelSpectrum subject, String[] query)
	{
		int[] c = Util.tryParseColors(query);
		
		if(c.length != 1) return false;
		
		subject.setBorderColor(c[0]);
		return true;
	}
	
	private boolean _processQuantize(pav.lib.visualizer.MelSpectrum subject, String[] query)
	{
		int[] q = Util.tryParseInts(query);
		
		if(q.length != 1) return false;
		
		subject.quantize(q[0]);
		return true;
	}
}
