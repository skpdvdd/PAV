
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

import java.io.File;
import java.nio.ByteOrder;
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
		
		Options options = new Options();
		options.addOption("renderer", true, "The Processing render mode to use.");
		options.addOption("width", true, "The width of the visualization window.");
		options.addOption("height", true, "The height of the visualization window.");
		options.addOption("resizable", false, "Whether the visualization window is resizable.");
		
		options.addOption("audiosource", true, "Audio source to use (socket or fifo).");
		options.addOption("sampleformat", true, "Sample format (int8 or float).");
		options.addOption("samplesize", true, "Number of samples per frame (512, 1024 or 2048)");
		options.addOption("byteorder", true, "Byte order of the samples (le or be)");
		
		options.addOption("path", true, "Path to the fifo the fifo audio source should use.");
		options.addOption("port", true, "Port the socket audio source should listen to.");
		
		CommandLineParser parser = new GnuParser();
		
		try {
			CommandLine cmd = parser.parse(options, args);
			
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
			
			if(cmd.hasOption("audiosource")) {
				if(cmd.getOptionValue("audiosource").equals(Config.AUDIO_SOURCE_FIFO)) {
					Config.audioSource = Config.AUDIO_SOURCE_FIFO;
				}
				else if(cmd.getOptionValue("audiosource").equals(Config.AUDIO_SOURCE_SOCKET)) {
					Config.audioSource = Config.AUDIO_SOURCE_SOCKET;
				}
				else {
					Console.error("Invalid audio source specified.");
				}
			}
			else {
				Console.out("No audio source specified, using " + Config.audioSource + ".");
			}
			
			if(cmd.hasOption("sampleformat")) {
				if(cmd.getOptionValue("sampleformat").equals(Config.SAMPLE_FORMAT_INT8)) {
					Config.sampleFormat = Config.SAMPLE_FORMAT_INT8;
				}
				else if(cmd.getOptionValue("sampleformat").equals(Config.SAMPLE_FORMAT_FLOAT)) {
					Config.sampleFormat = Config.SAMPLE_FORMAT_FLOAT;
				}
				else {
					Console.error("Invalid sample format specified.");
				}
			}
			else {
				Console.out("No sample format specified, using " + Config.sampleFormat + ".");
			}
			
			if(cmd.hasOption("samplesize")) {
				try {
					int sampleSize = Integer.parseInt(cmd.getOptionValue("samplesize"));
					
					if(sampleSize == 512 || sampleSize == 1024 || sampleSize == 2048) {
						Config.sampleSize = sampleSize;
					}
					else {
						Console.error("Invalid sample size specified.");
					}
				}
				catch (NumberFormatException e) {
					Console.error("Error while parsing command line arguments: samplesize is not a valid integer.");
				}
			}
			else {
				Console.out("No sample size specified, using " + Config.sampleSize + ".");
			}
			
			if(cmd.hasOption("byteorder")) {
				if(cmd.getOptionValue("byteorder").equals(Config.BYTE_ORDER_LE)) {
					Config.byteOrder = ByteOrder.LITTLE_ENDIAN;
				}
				else if(cmd.getOptionValue("byteorder").equals(Config.BYTE_ORDER_BE)) {
					Config.byteOrder = ByteOrder.BIG_ENDIAN;
				}
				else {
					Console.error("Invalid byte order specified.");
				}
			}
			else {
				Console.out("No byte order specified, using " + Config.BYTE_ORDER_LE + ".");
			}
			
			if(Config.audioSource.equals(Config.AUDIO_SOURCE_FIFO)) {
				if(cmd.hasOption("path")) {
					if(! (new File(cmd.getOptionValue("path"))).canRead()) {
						Console.error("Unable to read the specified FIFO, aborting.");
						return;
					}
					
					Config.fifoPath = cmd.getOptionValue("path");
				}
				else {
					Console.error("No fifo path specified, aborting.");
					return;
				}
			}
			
			if(Config.audioSource.equals(Config.AUDIO_SOURCE_SOCKET)) {
				if(cmd.hasOption("port")) {
					try {
						Config.socketPort = Integer.parseInt(cmd.getOptionValue("port"));
					}
					catch(NumberFormatException e) {
						Console.error("Error while parsing command line arguments: port is not a valid integer.");
					}
				}
				else {
					Console.out("No port specified, using " + Config.socketPort + ".");
				}
			}
		}
		catch (ParseException e) {
			Console.error("Error while parsing command line arguments: " + e.getMessage());
			new HelpFormatter().printHelp("pav", options);
		}
		
		PApplet.main(new String[] { "pav.PAV" });
	}
}
