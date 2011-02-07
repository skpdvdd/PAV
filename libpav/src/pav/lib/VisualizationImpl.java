
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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.TreeMap;
import pav.lib.frame.Frame;
import pav.lib.visualizer.Visualizer;
import processing.core.PApplet;

/**
 * Visualization implementation.
 * 
 * @author christopher
 */
public class VisualizationImpl implements Visualization
{
	private final PApplet _p;
	private final TreeMap<Integer, Visualizer> _visualizers;
	private final HashMap<String, Integer> _visualizerNames;
	
	/**
	 * Ctor.
	 * 
	 * @param target Where to visualize to. Must not be null
	 */
	public VisualizationImpl(PApplet target)
	{
		_p = target;
		_visualizers = new TreeMap<Integer, Visualizer>();
		_visualizerNames = new HashMap<String, Integer>();
	}
		
	/**
	 * Adds a new visualizer. The new visualizer will be drawn last.
	 * This method automatically tells the visualizer to draw to the applet of this visualization.
	 * 
	 * @param visualizer The visualizer to add. Must not be null
	 * @throws PAVException If an error occured while adding the visualizer
	 */
	@Override
	public void addVisualizer(Visualizer visualizer) throws PAVException
	{
		int level = 0;
		
		try {
			level = _visualizers.lastKey() + 1;
		}
		catch(NoSuchElementException e) { }
		
		addVisualizer(visualizer, level, visualizer + " [" + level + "]");
	}
	
	/**
	 * Adds a new visualizer. The new visualizer will be drawn last.
	 * This method automatically tells the visualizer to draw to the applet of this visualization.
	 * 
	 * @param visualizer The visualizer to add. Must not be null
	 * @param name The name of the visualizer. Must be unique
	 * @throws PAVException If an error occured while adding the visualizer
	 */
	@Override
	public void addVisualizer(Visualizer visualizer, String name) throws PAVException
	{
		int level = 0;
		
		try {
			level = _visualizers.lastKey() + 1;
		}
		catch(NoSuchElementException e) { }
		
		addVisualizer(visualizer, level, name);
	}
	
	/**
	 * Adds a new visualizer with a given level. The level specifies then the visualizer
	 * will be told to draw itself relative to the other visualizers. Visualizers with lower
	 * levels will be drawn first. If a visualizer with the given level already exists, it will be replaced.
	 * This method automatically tells the visualizer to draw to the applet of this visualization.
	 * 
	 * @param visualizer The visualizer to add. Must not be null
	 * @param level The level
	 * @throws PAVException If an error occured while adding the visualizer
	 */
	@Override
	public void addVisualizer(Visualizer visualizer, int level) throws PAVException
	{
		addVisualizer(visualizer, level, visualizer + " [" + level + "]");
	}
	
	/**
	 * Adds a new visualizer with a given level. The level specifies then the visualizer
	 * will be told to draw itself relative to the other visualizers. Visualizers with lower
	 * levels will be drawn first. If a visualizer with the given level already exists, it will be replaced.
	 * This method automatically tells the visualizer to draw to the applet of this visualization.
	 * 
	 * @param visualizer The visualizer to add. Must not be null
	 * @param level The level
	 * @param name The name of the visualizer. Must be unique
	 * @throws PAVException If an error occured while adding the visualizer
	 */
	@Override
	public void addVisualizer(Visualizer visualizer, int level, String name) throws PAVException
	{
		visualizer.drawTo(_p);
		
		if(_visualizers.containsKey(level)) {
			removeVisualizerAt(level);
		}
		
		_visualizers.put(level, visualizer);
		_visualizerNames.put(name, level);
	}
	
	@Override
	public void removeVisualizer(Visualizer visualizer)
	{
		Integer key = null;
		
		for(Map.Entry<Integer, Visualizer> e : _visualizers.entrySet()) {
			if(e.getValue().equals(visualizer)) {
				key = e.getKey();
				break;
			}
		}
		
		if(key != null) {
			removeVisualizerAt(key);
		}
	}
	
	@Override
	public void removeVisualizer(String name)
	{
		if(_visualizerNames.containsKey(name)) {
			removeVisualizerAt(_visualizerNames.get(name));
		}
	}
	
	@Override
	public void removeVisualizerAt(int level)
	{
		Visualizer remove = _visualizers.remove(level);
		remove.dispose();
		
		String key = null;
			
		for(Map.Entry<String, Integer> e : _visualizerNames.entrySet()) {
			if(e.getValue().equals(level)) {
				key = e.getKey();
				break;
			}
		}

		if(key != null) {
			_visualizerNames.remove(key);
		}
	}
	
	@Override
	public Visualizer getVisualizer(String name)
	{
		if(_visualizerNames.containsKey(name)) {
			return getVisualizer(_visualizerNames.get(name));
		}

		return null;
	}
	
	@Override
	public Visualizer getVisualizer(int level)
	{
		return _visualizers.get(level);
	}
	
	@Override
	public Map<Integer, Visualizer> getVisualizers()
	{
		return _visualizers;
	}
	
	@Override
	public int numVisualizers()
	{
		return _visualizers.size();
	}
	
	@Override
	public void setSampleRate(float rate)
	{
		Frame.setSampleRate(rate);
	}
	
	@Override
	public void process(float[] frame) throws PAVException
	{
		Frame.update(frame);
		
		for(Visualizer v : visualizers()) {
			v.process();
		}
	}
	
	/**
	 * Returns a sorted list of the visualizers to draw to or an empty
	 * set if no visualizers are added.
	 * 
	 * @return A list of visualizers
	 */
	protected List<Visualizer> visualizers()
	{
		LinkedList<Visualizer> vis = new LinkedList<Visualizer>();
		
		for(Visualizer v : _visualizers.values()) {
			vis.add(v);
		}
		
		return vis;
	}
}
