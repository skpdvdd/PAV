package pav.lib;

import java.util.HashMap;
import java.util.Map;
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
	private static Map<String, GLTextureFilter> _textureFilters;
	
	/**
	 * Initializes the shaders. Must be called before initializing any visualizers.
	 * 
	 * @param p The PApplet to use. Must not be null
	 * @throws PAVException On errors
	 */
	public static void initialize(PApplet p) throws PAVException
	{
		if(! (p.g instanceof GLGraphics)) {
			throw new PAVException("Shaders are only supported under the GLGraphics renderer.");
		}
		
		_textureFilters = new HashMap<String, GLTextureFilter>();
		_textureFilters.put("bubbles-blur", new GLTextureFilter(p, "shaders/bubbles/blur.xml"));
		_textureFilters.put("bubbles-ageupdate", new GLTextureFilter(p, "shaders/bubbles/ageupdate.xml"));
		_textureFilters.put("bubbles-blend", new GLTextureFilter(p, "shaders/bubbles/blend.xml"));
		_textureFilters.put("common-bloom", new GLTextureFilter(p, "shaders/common/bloom.xml"));
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
