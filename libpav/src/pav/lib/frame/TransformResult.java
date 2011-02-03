
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

package pav.lib.frame;

/**
 * A transformation result. The values must not be modified.
 * 
 * @author christopher
 */
public class TransformResult
{
	private final float[] _frame;
	private final float _min;
	private final float _max;
	
	/**
	 * Ctor.
	 * 
	 * @param frame The transformed frame
	 * @param min The min of the frame values
	 * @param max The max of the frame values
	 */
	public TransformResult(float[] frame, float min, float max)
	{
		_frame = frame;
		_min = min;
		_max = max;
	}
	
	/**
	 * The transformed frame. Must not be modified.
	 * 
	 * @return The result
	 */
	public float[] frame()
	{
		return _frame;
	}

	/**
	 * The minimum of the frame values.
	 * 
	 * @return The min value
	 */
	public float min()
	{
		return _min;
	}

	/**
	 * The maximum of the frame values.
	 * 
	 * @return The max value
	 */
	public float max()
	{
		return _max;
	}
}
