
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

package pav.player;

import processing.core.PConstants;

/**
 * Configuration used by PAV. This is a static class.
 * 
 * @author christopher
 */
final class Config
{
	/**
	 * Whether to stream data to PAV.
	 */
	public static boolean usePav = true;
	
	/**
	 * The host PAV runs on.
	 */
	public static String pavHost = "localhost";
		
	/**
	 * The port PAV runs on.
	 */
	public static int pavPort = 2198;
		
	/**
	 * The frame size, i.e., the number of samples per frame. Must be 512, 1024 or 2048.
	 */
	public static int frameSize = 1024;
	
	/**
	 * The renderer to use.
	 */
	public static String renderer = PConstants.JAVA2D;
	
	/**
	 * The player width.
	 */
	public static int width = 400;
	
	/**
	 * The player height.
	 */
	public static int height = 300;
	
	/**
	 * Whether the player is resizable.
	 */
	public static boolean resizable = false;
	
	private Config() { }
}
