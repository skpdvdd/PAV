
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
	public static final String AUDIO_SOURCE_SOCKET = "socket";
	
	/**
	 * Samples are in int8 (2 bytes, java short) format.
	 */
	public static final String SAMPLE_FORMAT_INT8 = "int8";
	
	/**
	 * Samples are in normalized float (4 bytes, -1 to 1) format.
	 */
	public static final String SAMPLE_FORMAT_FLOAT = "float";
	
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
	public static String audioSource = AUDIO_SOURCE_SOCKET;
	
	/**
	 * The sample format.
	 */
	public static String sampleFormat = SAMPLE_FORMAT_FLOAT;
	
	/**
	 * The sample size;
	 */
	public static int sampleSize = 1024;
	
	/**
	 * The byte order.
	 */
	public static ByteOrder byteOrder = ByteOrder.LITTLE_ENDIAN;
	
	/**
	 * The port the socket audio source should listen to.
	 */
	public static int socketPort = 2198;
	
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
	 * Whether the display window is resizable.
	 */
	public static boolean windowResizable = false;
	
	/**
	 * The renderer to use.
	 */
	public static String renderer = PConstants.P2D;
	
	private Config() { }
}
