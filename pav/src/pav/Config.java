
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

import processing.core.PConstants;

/**
 * Configuration used by PAV. This is a static class.
 * 
 * @author christopher
 */
public final class Config
{
	/**
	 * Use MPD audio source.
	 */
	public static final String AUDIO_SOURCE_MPD = "mpd";
	
	/**
	 * Use socket audio source.
	 */
	public static final String AUDIO_SOURCE_SOCKET = "socket";
	
	/**
	 * The audio source to use.
	 */
	public static String audioSource = AUDIO_SOURCE_SOCKET;
		
	/**
	 * The width of the display window.
	 */
	public static int windowWidth = 800;
	
	/**
	 * The height of the display window.
	 */
	public static int windowHeight = 600;
	
	/**
	 * Whether the display window is resizable.
	 */
	public static boolean windowResizable = false;
	
	/**
	 * The renderer to use.
	 */
	public static String renderer = PConstants.P2D;
	
	public static final class SocketAudioSource
	{
		/**
		 * The port to listen to for incoming connections.
		 */
		public static int port = 2198;
	}
	
	/**
	 * MPDAudioSource config.
	 * 
	 * @author christopher
	 */
	public static final class MPDAudioSource
	{
		/**
		 * The sample size;
		 */
		public static int sampleSize = 2048;
		
		/**
		 * The path to the fifo.
		 */
		public static String fifoPath = "/tmp/mpd.fifo";
	}
	
	private Config() { }
}
