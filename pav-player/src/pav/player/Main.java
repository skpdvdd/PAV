
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

import java.io.ByteArrayInputStream;
import java.util.logging.LogManager;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import processing.core.PApplet;

/**
 * Entry point of the program.
 * 
 * @author christopher
 */
final class Main
{
	/**
	 * Entry point of the program.
	 * 
	 * @param args Startup arguments
	 */
	public static void main(String[] args)
	{
		System.out.println("----------");
		System.out.println("PAV Player");
		System.out.println("----------\n");
		
		_initConfig(args);
		
		try {
			LogManager.getLogManager().readConfiguration(new ByteArrayInputStream("org.jaudiotagger.level = OFF".getBytes()));
		}
		catch (Exception e) {
			Console.error("Error while disabling jAudioTagger logging: " + e.getMessage());
		}
		
		PApplet.main(new String[] { "pav.player.Player" });
	}
	
	private static void _initConfig(String[] args)
	{
		Options options = new Options();
		options.addOption("nopav", false, "Do not try to send audio data to PAV.");
		options.addOption("pavhost", true, "The host PAV is running on.");
		options.addOption("pavport", true, "The port PAV is running on.");
		options.addOption("renderer", true, "The Processing render mode to use.");
		options.addOption("framesize", true, "The size of the audio frames. Must be 512, 1024 or 2048.");
		options.addOption("width", true, "The player width.");
		options.addOption("height", true, "The player height.");
		options.addOption("resizable", false, "Make the player resizable.");
		
		CommandLineParser parser = new GnuParser();
		
		try {
			CommandLine cmd = parser.parse(options, args);
			
			if(cmd.hasOption("nopav")) {
				Config.usePav = false;
				Console.out("PAV support disabled.");
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
					int width = Integer.parseInt(option);
					
					if(width > 0) {
						Config.width = width;
					}
					else {
						Console.error("Window width must be positive.");
					}
				}
				catch (NumberFormatException e) {
					Console.error("Invalid window width specified.");
				}
			}
			else {
				Console.out("No window width specified, using " + Config.width + ".");
			}
			
			if(cmd.hasOption("height")) {
				String option = cmd.getOptionValue("height");
				
				try {
					int height = Integer.parseInt(option);
					
					if(height > 0) {
						Config.height = height;
					}
					else {
						Console.error("Window height must be positive.");
					}
				}
				catch (NumberFormatException e) {
					Console.error("Invalid window height specified.");
				}
			}
			else {
				Console.out("No window height specified, using " + Config.height + ".");
			}
			
			if(cmd.hasOption("resizable")) {
				Config.resizable = true;
			}
			
			if(Config.usePav) {
				if(cmd.hasOption("pavhost")) {
					Config.pavHost = cmd.getOptionValue("pavhost");
				}
				else {
					Console.out("No PAV host specified, using " + Config.pavHost + ".");
				}
				
				if(cmd.hasOption("pavport")) {
					String option = cmd.getOptionValue("pavport");
					
					try {
						int port = Integer.parseInt(option);
						
						if(port > 1023 && port < 65536) {
							Config.pavPort = port;
						}
						else {
							Console.error("PAV port must be between 1024 and 65535.");
						}
					}
					catch (NumberFormatException e) {
						Console.error("Invalid PAV port specified.");
					}
				}
				else {
					Console.out("No PAV port specified, using " + Config.pavPort + ".");
				}
				
				if(cmd.hasOption("framesize")) {
					String option = cmd.getOptionValue("framesize");
					
					try {
						int frameSize = Integer.parseInt(option);
						
						if(frameSize == 512 || frameSize == 1024 || frameSize == 2048) {
							Config.frameSize = frameSize;
						}
						else {
							Console.error("Frame size must be 512, 1024 or 2048.");
						}
					}
					catch(NumberFormatException e) {
						Console.error("Invalid frame size specified.");
					}
				}
				else {
					Console.out("No frame size specified, using " + Config.frameSize + ".");
				}
			}
		}
		catch(ParseException e) {
			Console.error("Error while parsing command line arguments: " + e.getMessage());
			new HelpFormatter().printHelp("pav-player", options);
		}
	}
	
	private Main() { }
}
