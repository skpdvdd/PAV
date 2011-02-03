
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

package pav.lib;

import java.util.Map;
import pav.lib.visualizer.Visualizer;

/**
 * A visualization, comprised of a number of visualizers.
 * 
 * @author christopher
 */
public interface Visualization
{
	/**
	 * Adds a new visualizer. The new visualizer will be drawn last.
	 * 
	 * @param visualizer The visualizer to add. Must not be null
	 */
	void addVisualizer(Visualizer visualizer);
	
	/**
	 * Adds a new visualizer. The new visualizer will be drawn last.
	 * 
	 * @param visualizer The visualizer to add. Must not be null
	 * @param name The name of the visualizer. Must be unique
	 */
	void addVisualizer(Visualizer visualizer, String name);
	
	/**
	 * Adds a new visualizer with a given level. The level specifies then the visualizer
	 * will be told to draw itself relative to the other visualizers. Visualizers with lower
	 * levels will be drawn first. If a visualizer with the given level already exists, it will be replaced.
	 * 
	 * @param visualizer The visualizer to add. Must not be null
	 * @param level The level
	 */
	void addVisualizer(Visualizer visualizer, int level);
	
	/**
	 * Adds a new visualizer with a given level. The level specifies then the visualizer
	 * will be told to draw itself relative to the other visualizers. Visualizers with lower
	 * levels will be drawn first. If a visualizer with the given level already exists, it will be replaced.
	 * 
	 * @param visualizer The visualizer to add. Must not be null
	 * @param level The level
	 * @param name The name of the visualizer. Must be unique
	 */
	void addVisualizer(Visualizer visualizer, int level, String name);
	
	/**
	 * Removes a visualizer. Does nothing if the visualizer does not exist.
	 * 
	 * @param visualizer The visualizer. Must not be null
	 */
	void removeVisualizer(Visualizer visualizer);
	
	/**
	 * Removes the visualizer with the given name. Does nothing if the visualizer does not exist.
	 * 
	 * @param name The name of the visualizer. Must not be null
	 */
	void removeVisualizer(String name);
	
	/**
	 * Removes the visualizer at the specified level if it exists, otherwise does nothing.
	 * 
	 * @param level The level
	 */
	void removeVisualizerAt(int level);
	
	/**
	 * Returns the visualizer that is located at the specified level. Returns null if
	 * no visualizer exists at that level.
	 * 
	 * @param level The level
	 * @return The visualizer or null
	 */
	Visualizer getVisualizer(int level);
	
	/**
	 * Returns the visualizer with the given name or null if no such visualizer exists.
	 * 
	 * @param level The visualizer name
	 * @return The visualizer or null
	 */
	Visualizer getVisualizer(String name);
	
	/**
	 * Gets all visualizers that are currently part of this visualization along
	 * with their associated level. The Map is be sorted, the visualizer with the
	 * lowest level is the first item in the list.
	 * 
	 * @return A map containing all visualizers
	 */
	Map<Integer, Visualizer> getVisualizers();
	
	/**
	 * Sets the audio sample rate.
	 * 
	 * @param rate The sample rate. Must be > 0
	 */
	void setSampleRate(float rate);
	
	/**
	 * Tells the visualization to process.
	 * 
	 * @param frame The next frame of the audio signal. Must not be null
	 * @throws PAVException On any errors
	 */	
	void process(float[] frame) throws PAVException;
}
