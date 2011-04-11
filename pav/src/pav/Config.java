
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

import java.nio.ByteOrder;
import processing.core.PConstants;

/**
 * Configuration used by PAV. This is a static class.
 * 
 * @author christopher
 */
public final class Config
{
	/**
	 * Use FIFO audio source.
	 */
	public static final String AUDIO_SOURCE_FIFO = "fifo";
	
	/**
	 * Use socket audio source.
	 */
	public static final String AUDIO_SOURCE_UDP = "udp";
	
	/**
	 * Audio data are transfered as little-endian byte stream.
	 */
	public static final String BYTE_ORDER_LE = "le";
	
	/**
	 * Audio data are transfered as big-endian byte stream.
	 */
	public static final String BYTE_ORDER_BE = "be";
	
	/**
	 * The audio source to use.
	 */
	public static String audioSource = AUDIO_SOURCE_UDP;
	
	/**
	 * The sample size. Must be 512, 1024 or 2048.
	 */
	public static int sampleSize = 1024;
	
	/**
	 * The sample rate.
	 */
	public static int sampleRate = 44100;
	
	/**
	 * The byte order.
	 */
	public static ByteOrder byteOrder = ByteOrder.LITTLE_ENDIAN;
	
	/**
	 * The port the udp audio source should listen to.
	 */
	public static int udpPort = 2198;
	
	/**
	 * The path to the fifo the fifo audio source should use.
	 */
	public static String fifoPath = "";
		
	/**
	 * The width of the display window.
	 */
	public static int windowWidth = 1024;
	
	/**
	 * The height of the display window.
	 */
	public static int windowHeight = 768;
	
	/**
	 * The renderer to use.
	 */
	public static String renderer = PConstants.P2D;
	
	private Config() { }
}
