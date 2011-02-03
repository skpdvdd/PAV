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
