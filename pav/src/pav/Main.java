package pav;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import processing.core.PApplet;

public class Main
{
	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		System.out.println("------------------------------");
		System.out.println("Processing Audio Visualization");
		System.out.println("------------------------------\n");
		
		_initConfig(args);
		
		PApplet.main(new String[] {"pav.PAV"});
	}
	
	private static void _initConfig(String[] args)
	{
		Options options = new Options();
		options.addOption("port", true, "The port of the RMI registry.");
		options.addOption("renderer", true, "The Processing render mode to use.");
		options.addOption("width", true, "The width of the visualization window.");
		options.addOption("height", true, "The height of the visualization window.");
		options.addOption("resizable", false, "Whether the visualization window is resizable.");
		
		CommandLineParser parser = new GnuParser();
		
		try {
			CommandLine cmd = parser.parse(options, args);
			
			if(cmd.hasOption("port")) {
				String option = cmd.getOptionValue("port");
				
				try {
					Config.port = Integer.parseInt(option);
				}
				catch(NumberFormatException e) {
					Console.error("Error while parsing command line arguments: port is not a valid integer.");
				}
			}
			else {
				Console.out("No port specified, using " + Config.port + ".");
			}
			
			if(cmd.hasOption("renderer")) {
				Config.renderer = cmd.getOptionValue("renderer");
			}
			else {
				Console.out("No render mode specified, using " + Config.renderer + ".");
			}
			
			if(cmd.hasOption("width")) {
				String option = cmd.getOptionValue("width");
				
				try {
					Config.windowWidth = Integer.parseInt(option);
				}
				catch (NumberFormatException e) {
					Console.error("Error while parsing command line arguments: width is not a valid integer.");
				}
			}
			else {
				Console.out("No window width specified, using " + Config.windowWidth + ".");
			}
			
			if(cmd.hasOption("height")) {
				String option = cmd.getOptionValue("height");
				
				try {
					Config.windowHeight = Integer.parseInt(option);
				}
				catch (NumberFormatException e) {
					Console.error("Error while parsing command line arguments: height is not a valid integer.");
				}
			}
			else {
				Console.out("No window height specified, using " + Config.windowHeight + ".");
			}
			
			if(cmd.hasOption("resizable")) {
				Config.windowResizable = true;
			}
		}
		catch (ParseException e) {
			Console.error("Error while parsing command line arguments: " + e.getMessage());
			new HelpFormatter().printHelp("pav", options);
		}
	}
}
