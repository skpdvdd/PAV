package pav;

import java.util.Arrays;
import processing.core.PApplet;

/**
 * Misc utility functionality. This is a static class.
 * 
 * @author christopher
 */
public class Util
{
	/**
	 * Returns a copy of in, but without the first element.
	 * 
	 * @param in The array to copy. Must not be null
	 * @return The copy
	 */
	public static String[] removeFirst(String[] in)
	{
		if(in.length == 0) {
			return new String[0];
		}
		
		return Arrays.copyOfRange(in, 1, in.length);
	}
	
	/**
	 * Parses an array of integers.
	 * 
	 * @param in The input array. Must not be null
	 * @return The result
	 * @throws NumberFormatException If any element in in is not parsable
	 */
	public static int[] parseInts(String[] in)
	{
		int[] out = new int[in.length];
		
		for(int i = 0; i < in.length; i++) {
			out[i] = Integer.parseInt(in[i]);
		}
		
		return out;
	}
	
	/**
	 * Parses an array of floats.
	 * 
	 * @param in The input array. Must not be null
	 * @return The result
	 * @throws NumberFormatException If any element in in is not parsable
	 */
	public static float[] parseFloats(String[] in)
	{
		float[] out = new float[in.length];
		
		for(int i = 0; i < in.length; i++) {
			out[i] = Float.parseFloat(in[i]);
		}
		
		return out;
	}
	
	/**
	 * Parses an array of colors (RRGGBB or AARRGGBB).
	 * 
	 * @param in The input array. Must not be null
	 * @return The result
	 * @throws NumberFormatException If any element in in is not parsable
	 */
	public static int[] parseColors(String[] in)
	{
		int[] out = new int[in.length];
		
		for(int i = 0; i < in.length; i++) {
			String c = (in[i].length() == 6) ? "FF" + in[i] : in[i];

			if(c.length() != 8) throw new NumberFormatException();
			
			out[i] = PApplet.unhex(c);
		}
		
		return out;
	}
	
	/**
	 * Parses an array of booleans.
	 * 
	 * 1 or true (ignoring case) will result in true, everything else in false.
	 * 
	 * @param in The input array. Must not be null
	 * @return The result
	 */
	public static boolean[] parseBools(String[] in)
	{
		boolean[] out = new boolean[in.length];
		
		for(int i = 0; i < in.length; i++) {
			String b = in[i];
			
			if(b.equals("0")) b = "false";
			if(b.equals("1")) b = "true";
			
			out[i] = Boolean.parseBoolean(b);
		}
		
		return out;
	}
	
	/**
	 * Tries to parse an array of integers.
	 * 
	 * Returns the parsed version of in or an empty array on any parse errors.
	 * 
	 * @param in The input array. Must not be null
	 * @return The result
	 */
	public static int[] tryParseInts(String[] in)
	{
		try {
			return parseInts(in);
		}
		catch(NumberFormatException e) {
			return new int[0];
		}
	}
	
	/**
	 * Tries to parse an array of floats.
	 * 
	 * Returns the parsed version of in or an empty array on any parse errors.
	 * 
	 * @param in The input array. Must not be null
	 * @return The result
	 */
	public static float[] tryParseFloats(String[] in)
	{
		try {
			return parseFloats(in);
		}
		catch(NumberFormatException e) {
			return new float[0];
		}
	}
	
	/**
	 * Tries to parse an array of colors (RRGGBB or AARRGGBB).
	 * 
	 * Returns the parsed version of in or an empty array on any parse errors.
	 * 
	 * @param in The input array. Must not be null
	 * @return The result
	 */
	public static int[] tryParseColors(String[] in)
	{
		try {
			return parseColors(in);
		}
		catch(NumberFormatException e) {
			return new int[0];
		}
	}
	
	private Util() { }
}
