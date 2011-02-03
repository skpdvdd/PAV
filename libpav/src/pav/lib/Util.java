
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

package pav.lib;

import processing.core.PApplet;

/**
 * Misc utility functions. This is a static class.
 * 
 * @author christopher
 */
public class Util
{
	/**
	 * Quantizes values to a number of integers, e.g. if data is {3,4,5,6} and steps
	 * is 2 the result will be {1.0,1.0,2.0,2.0}. The lowest value in data will always map to 1.0,
	 * the largest to steps. 
	 * 
	 * @param data The data to quantize. Must not be null
	 * @param steps The number of steps. Must be > 0
	 * @return The quantized data
	 */
	public static float[] quantize(float[] data, int steps)
	{
		float[] mm = calcMinMax(data);
		
		return quantize(data, steps, mm[0], mm[1]);
	}
	
	/**
	 * Quantizes values to a number of integers, e.g. if data is {3,4,5,6} and steps
	 * is 2 the result will be {1.0,1.0,2.0,2.0}. The lowest value in data will always map to 1,
	 * the largest to steps. Use this version if you know the min and max values of the data set.
	 * 
	 * @param data The data to quantize. Must not be null
	 * @param steps The number of steps. Must be > 0
	 * @param dataMin The minimum value in data
	 * @param dataMax The maximum value in data
	 * @return The quantized data
	 */
	public static float[] quantize(float[] data, int steps, float dataMin, float dataMax)
	{
		int len = data.length;
		float[] out = new float[len];
		
		for(int i = 0; i < len; i++) {
			out[i] = Math.round(PApplet.map(data[i], dataMin, dataMax, 1, steps));
		}
		
		return out;
	}
	
	/**
	 * Calculates the minimum and maximum of a data set.
	 * 
	 * @param data The data set. Must not be null
	 * @return An array of the form { min, max }
	 */
	public static float[] calcMinMax(float[] data)
	{
		int len = data.length;
		float min = Float.MAX_VALUE;
		float max = Float.MIN_VALUE;

		for(int i = 0; i < len; i++) {
			float v = data[i];
			
			if(v < min) min = v;
			if(v > max) max = v;
		}
		
		return new float[] { min, max };
	}
	
	private Util() { }
}
