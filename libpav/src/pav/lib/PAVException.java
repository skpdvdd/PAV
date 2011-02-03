
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

/**
 * Base class of all PAV-specific exceptions.
 * 
 * @author christopher
 */
public class PAVException extends Exception
{
	private static final long serialVersionUID = 4985787077612555714L;
	
	/**
	 * Ctor.
	 */
	public PAVException()
	{
		super();
	}
	
	/**
	 * Ctor.
	 * 
	 * @param message The error message
	 */
	public PAVException(String message)
	{
		super(message);
	}
	
	/**
	 * Ctor.
	 * 
	 * @param cause The cause of the error
	 */
	public PAVException(Throwable cause)
	{
		super(cause);
	}
	
	/**
	 * Ctor.
	 * 
	 * @param message The error message
	 * @param cause The cause of the error
	 */
	public PAVException(String message, Throwable cause)
	{
		super(message, cause);
	}
}
