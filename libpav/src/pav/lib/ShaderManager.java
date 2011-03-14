
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

import java.io.File;
import java.io.FilenameFilter;
import java.util.HashMap;
import processing.core.PApplet;
import codeanticode.glgraphics.GLGraphics;
import codeanticode.glgraphics.GLTextureFilter;

/**
 * Manages GLGraphics shaders.
 * 
 * @author christopher
 */
public class ShaderManager
{
	private static HashMap<String, GLTextureFilter> _textureFilters;
	
	/**
	 * Initializes the shaders. Must be called before initializing any visualizers.
	 * 
	 * @param p The PApplet to use. Must not be null
	 * @throws PAVException On errors
	 */
	public static void initialize(PApplet p) throws PAVException
	{
		_textureFilters = new HashMap<String, GLTextureFilter>();
		
		if(! (p.g instanceof GLGraphics)) {
			throw new PAVException("Shaders are only supported under the GLGraphics renderer.");
		}
				
	    File s = new File("data/shaders");
	    
	    if(s.canRead()) {
	    	String[] shaders = s.list(new FilenameFilter()
			{
				@Override
				public boolean accept(File dir, String name)
				{
					return new File(dir, name).isFile() && name.toLowerCase().endsWith(".xml");
				}
			});
	    	
	    	for(String shader : shaders) {
	    		_textureFilters.put(shader.substring(0, shader.length() - 4), new GLTextureFilter(p, "shaders/" + shader));
	    	}
	    }
	    else {
	    	throw new PAVException("No shaders found.");
	    }
	}
	
	/**
	 * Returns a texture filter.
	 * 
	 * Texture filters are shared by all visualizers, therefore settings might be changed by other visualizers.
	 * 
	 * @param name The name of the texture filter. Must not be null
	 * @return The filter
	 * @throws PAVException If the filter name is unknown
	 */
	public static GLTextureFilter getTextureFilter(String name) throws PAVException
	{
		if(! _textureFilters.containsKey(name)) {
			throw new PAVException("Texture filter '" + name + "' not found.");
		}
		
		return _textureFilters.get(name);
	}
	
	private ShaderManager() { }
}
