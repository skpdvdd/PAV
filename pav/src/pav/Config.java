package pav;

import processing.core.PConstants;

/**
 * Configuration used by PAV. This is a static class.
 * 
 * @author christopher
 */
final class Config
{
	/**
	 * The port to listen to for incoming connections.
	 */
	public static int port = 2198;
	
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
	
	private Config() { }
}
