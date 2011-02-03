
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

import java.io.Serializable;
import processing.core.PApplet;

/**
 * Maps values to colors.
 * 
 * @author christopher
 */
public class ColorMapper implements Serializable
{
	private static final long serialVersionUID = -2737277542090976648L;
	
	private ColorMapperImpl _mapper;
	private int _mode;
	private float _vMin, _vMax;
		
	/**
	 * Sets the static color to use.
	 * 
	 * @param color The color
	 */
	public void setColor(int color)
	{
		Constant c = new Constant();
		c.setColor(color);
		
		_mapper = c;
	}
	
	/**
	 * Sets two colors to interpolate between.
	 * 
	 * @param a The color to start from
	 * @param b The color to interpolate to
	 * @param mode The color mode to use. Must be either PApplet.RGB or PApplet.HSB
	 */
	public void setColor(int a, int b, int mode)
	{
		TwoColors c = new TwoColors();
		c.setColor(a, b);
		
		_mode = mode;
		_mapper = c;
	}
	
	/**
	 * Sets the colors to interpolate between.
	 * 
	 * @param thresholds The relative thresholds to use. Values must be between 0 and 1 and sorted. The first element must be 0, the last 1. Must be of same length as colors
	 * @param colors The The colors to use. Must be of same length as thresholds
	 * @param mode The color mode to use. Must be either PApplet.RGB or PApplet.HSB
	 */
	public void setColor(float[] thresholds, int[] colors, int mode)
	{
		Colors c = new Colors();
		c.setColor(thresholds, colors);
		
		_mode = mode;
		_mapper = c;
	}
	
	/**
	 * Sets the minimum and maximum value.
	 * 
	 * @param min The minimum value
	 * @param max The maximum value
	 */
	public void setRange(float min, float max)
	{
		_vMin = min;
		_vMax = max;
	}
	
	/**
	 * Maps a value to a color.
	 * 
	 * @param value The value
	 * @return The color
	 */
	public int map(float value)
	{
		return _mapper.map(value);
	}
	
	/**
	 * A color mapper.
	 * 
	 * @author christopher
	 */
	private abstract class ColorMapperImpl implements Serializable
	{
		private static final long serialVersionUID = -1018124658424277468L;

		/**
		 * Maps a value to a color.
		 * 
		 * @param value The value
		 * @return The color
		 */
		public abstract int map(float value);
	}
	
	/**
	 * A constant color mapper.
	 * 
	 * @author christopher
	 */
	private class Constant extends ColorMapperImpl
	{
		private static final long serialVersionUID = 7034468912657552137L;
		
		private int _color;
		
		/**
		 * Sets the static color to use.
		 * 
		 * @param color The color
		 */
		public void setColor(int color)
		{
			_color = color;
		}
		
		/**
		 * Maps a value to a color.
		 * 
		 * @param value The value
		 * @return The color
		 */
		@Override
		public int map(float value)
		{
			return _color;
		}
	}
	
	/**
	 * Interpolates between two colors.
	 * 
	 * @author christopher
	 */
	private class TwoColors extends ColorMapperImpl
	{
		private static final long serialVersionUID = -5054720378481970404L;
		
		private int _a, _b;
		
		/**
		 * Sets two colors to interpolate between.
		 * 
		 * @param a The color to start from
		 * @param b The color to interpolate to
		 */
		public void setColor(int a, int b)
		{
			_a = a;
			_b = b;
		}

		/**
		 * Maps a value to a color.
		 * 
		 * @param value The value
		 * @return The color
		 */
		@Override
		public int map(float value)
		{
			float f = PApplet.map(value, _vMin, _vMax, 0, 1);
			
			return PApplet.lerpColor(_a, _b, f, _mode);
		}
	}
	
	/**
	 * Interpolates between an arbitrary number of colors.
	 * 
	 * @author christopher
	 */
	private class Colors extends ColorMapperImpl
	{
		private static final long serialVersionUID = -2672613831235217060L;
		
		private float[] _thresholds;
		private int[] _colors;
		
		/**
		 * Sets the colors to interpolate between.
		 * 
		 * @param thresholds The relative thresholds to use. Values must be between 0 and 1 and sorted. The first element must be 0, the last 1. Must be of same length as colors
		 * @param colors The The colors to use. Must be of same length as thresholds
		 */
		public void setColor(float[] thresholds, int[] colors)
		{
			_thresholds = thresholds;
			_colors = colors;
		}

		/**
		 * Maps a value to a color.
		 * 
		 * @param value The value
		 * @return The color
		 */
		@Override
		public int map(float value)
		{
			int cA = 0, cB = 0, l = _thresholds.length - 1;
			float tA = 0, tB = 0;

			for(int i = 0; i < l; i++) {
				cA = _colors[i];
				cB = _colors[i + 1];
				tA = _vMax * _thresholds[i];
				tB = _vMax * _thresholds[i + 1];
				
				if(value < tB) {
					break;
				}
			}
			
			float f = PApplet.map(value, tA, tB, 0, 1);
			
			return PApplet.lerpColor(cA, cB, f, _mode);
		}
	}
}
