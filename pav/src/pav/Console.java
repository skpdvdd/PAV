
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

package pav;

/**
 * Convenience functions for working with the console.
 * 
 * @author christopher
 */
public class Console
{
	/**
	 * Sends a message to the console.
	 * 
	 * @param message The message to send. Must not be null
	 */
	public static void out(String message)
	{
		System.out.println(" " + message);
	}
	
	/**
	 * Sends an error message to the console.
	 * 
	 * @param message The message to send. Must not be null
	 */
	public static void error(String message)
	{
		System.out.println("\n !!! " + message);
	}
	
	/**
	 * Generates an error message and sends it to the console.
	 * 
	 * @param cause The error
	 */
	public static void error(Throwable cause)
	{
		System.out.println("\n !!! Error: " + cause.getMessage());
		System.out.println(" ----- TRACE START -----");
		
		for(StackTraceElement e : cause.getStackTrace()) {
			System.out.println(" " + e.toString());
		}
		
		System.out.println(" ----- TRACE END -----");
	}
}
