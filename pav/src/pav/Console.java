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
