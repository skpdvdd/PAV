
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
 * Configurator for the Wavering visualizer.
 * 
 * @author christopher
 */
public class Wavering extends ConfiguratorAbstract
{
	@Override
	public boolean process(Visualizer subject, String query)
	{
		if(! (subject instanceof pav.lib.visualizer.Wavering)) {
			return false;
		}
		
		String[] q = query.split(" ");
		
		if(q.length < 2) {
			return false;
		}
		
		if(q[0].equals("sw")) {
			return _processStrokeWeight((pav.lib.visualizer.Wavering) subject, Util.removeFirst(q));
		}
		
		if(q[0].equals("radius")) {
			return _processRadius((pav.lib.visualizer.Wavering) subject, Util.removeFirst(q));
		}
		
		if(q[0].equals("displace")) {
			return _processDisplace((pav.lib.visualizer.Wavering) subject, Util.removeFirst(q));
		}
		
		if(q[0].equals("rcolor")) {
			return _processRcolor((pav.lib.visualizer.Wavering) subject, Util.removeFirst(q));
		}
		
		if(q[0].equals("darken")) {
			return _processDarken((pav.lib.visualizer.Wavering) subject, Util.removeFirst(q));
		}
		
		if(q[0].equals("brightthresh")) {
			return _processBrightthresh((pav.lib.visualizer.Wavering) subject, Util.removeFirst(q));
		}
		
		if(q[0].equals("bloomintensity")) {
			return _processBloomintensity((pav.lib.visualizer.Wavering) subject, Util.removeFirst(q));
		}
		
		if(q[0].equals("exposure")) {
			return _processExposure((pav.lib.visualizer.Wavering) subject, Util.removeFirst(q));
		}
		
		if(q[0].equals("maxbright")) {
			return _processMaxbright((pav.lib.visualizer.Wavering) subject, Util.removeFirst(q));
		}
		
		return false;
	}

	private boolean _processStrokeWeight(pav.lib.visualizer.Wavering subject, String[] query)
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

	private boolean _processRadius(pav.lib.visualizer.Wavering subject, String[] query)
	{
		float[] radius = Util.tryParseFloats(query);
		
		switch(radius.length) {
			case 1 :
				if(radius[0] > 0) {
					subject.setRadius(radius[0], radius[0]);
					return true;
				}
				else {
					return false;
				}
			case 2 :
				if(radius[0] > 0 && radius[1] > 0) {
					subject.setRadius(radius[0], radius[1]);
					return true;
				}
				else {
					return false;
				}
			default :
				return false;
		}
	}
	
	private boolean _processDisplace(pav.lib.visualizer.Wavering subject, String[] query)
	{
		float[] displace = Util.tryParseFloats(query);
		
		switch(displace.length) {
			case 1 :
				if(displace[0] > 0) {
					subject.setDisplacement(displace[0], displace[0]);
					return true;
				}
				else {
					return false;
				}
			case 2 :
				if(displace[0] > 0 && displace[1] > 0) {
					subject.setDisplacement(displace[0], displace[1]);
					return true;
				}
				else {
					return false;
				}
			default :
				return false;
		}
	}
	
	private boolean _processRcolor(pav.lib.visualizer.Wavering subject, String[] query)
	{
		if(! (subject.getImplementation() instanceof pav.lib.visualizer.Wavering.Fancy)) {
			return false;
		}
		
		pav.lib.visualizer.Wavering.Fancy impl = (pav.lib.visualizer.Wavering.Fancy) subject.getImplementation();
		
		int[] color = Util.tryParseColors(query);
		
		if(color.length == 1) {
			impl.setRingColor(color[0]);
			return true;
		}
		
		return false;
	}
	
	private boolean _processDarken(pav.lib.visualizer.Wavering subject, String[] query)
	{
		if(! (subject.getImplementation() instanceof pav.lib.visualizer.Wavering.Fancy)) {
			return false;
		}
		
		pav.lib.visualizer.Wavering.Fancy impl = (pav.lib.visualizer.Wavering.Fancy) subject.getImplementation();
		
		float[] darken = Util.tryParseFloats(query);
		
		if(darken.length == 1 && darken[0] > 0 && darken[0] <= 1) {
			impl.setDarkenFactor(darken[0]);
			return true;
		}
		
		return false;
	}
	
	private boolean _processBrightthresh(pav.lib.visualizer.Wavering subject, String[] query)
	{
		if(! (subject.getImplementation() instanceof pav.lib.visualizer.Wavering.Fancy)) {
			return false;
		}
		
		pav.lib.visualizer.Wavering.Fancy impl = (pav.lib.visualizer.Wavering.Fancy) subject.getImplementation();

		float[] brightthresh = Util.tryParseFloats(query);
		
		if(brightthresh.length == 1 && brightthresh[0] > 0 && brightthresh[0] < 1) {
			impl.setBrightnessThreshold(brightthresh[0]);
			return true;
		}
		
		return false;		
	}
	
	private boolean _processBloomintensity(pav.lib.visualizer.Wavering subject, String[] query)
	{
		if(! (subject.getImplementation() instanceof pav.lib.visualizer.Wavering.Fancy)) {
			return false;
		}
		
		pav.lib.visualizer.Wavering.Fancy impl = (pav.lib.visualizer.Wavering.Fancy) subject.getImplementation();
		
		float[] intensity = Util.tryParseFloats(query);
		
		if(intensity.length == 1 && intensity[0] >= 0) {
			impl.setBloomIntensity(intensity[0]);
			return true;
		}
		
		return false;
	}
	
	private boolean _processExposure(pav.lib.visualizer.Wavering subject, String[] query)
	{
		if(! (subject.getImplementation() instanceof pav.lib.visualizer.Wavering.Fancy)) {
			return false;
		}
		
		pav.lib.visualizer.Wavering.Fancy impl = (pav.lib.visualizer.Wavering.Fancy) subject.getImplementation();
		
		float[] exposure = Util.tryParseFloats(query);
		
		if(exposure.length == 1 && exposure[0] > 0) {
			impl.setTonemapExposure(exposure[0]);
			return true;
		}
		
		return false;
	}
	
	private boolean _processMaxbright(pav.lib.visualizer.Wavering subject, String[] query)
	{
		if(! (subject.getImplementation() instanceof pav.lib.visualizer.Wavering.Fancy)) {
			return false;
		}
		
		pav.lib.visualizer.Wavering.Fancy impl = (pav.lib.visualizer.Wavering.Fancy) subject.getImplementation();
		
		float[] maxbright = Util.tryParseFloats(query);
		
		if(maxbright.length == 1 && maxbright[0] > 0) {
			impl.setTonemapMaxBrightness(maxbright[0]);
			return true;
		}
		
		return false;
	}
}
