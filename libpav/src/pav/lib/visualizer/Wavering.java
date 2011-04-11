
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

package pav.lib.visualizer;

import pav.lib.PAVException;
import pav.lib.ShaderManager;
import pav.lib.frame.Frame;
import processing.core.PApplet;
import processing.core.PGraphics;
import codeanticode.glgraphics.GLGraphics;
import codeanticode.glgraphics.GLGraphicsOffScreen;
import codeanticode.glgraphics.GLTexture;
import codeanticode.glgraphics.GLTextureFilter;

/**
 * Creates a bright ring based on the waveform of the audio signal.
 * 
 * @author christopher
 */
public class Wavering extends VisualizerAbstract
{
	private static final long serialVersionUID = 7540082043727857258L;

	private float _strokeWeight;
	private boolean _colorAbsolute;
	private float _rx, _ry, _dx, _dy;
	private WaveringImpl _implementation;
	private int _interpolate, _width, _height;

	/**
	 * Ctor.
	 */
	public Wavering()
	{
		autosize();
		interpolate(200);
		setStrokeWeight(2);
		colorAbsolute(true);
		setColor(0xFF51610D, 0xFF52A417, PApplet.RGB);
	}
	
	@Override
	public void drawTo(PApplet applet) throws PAVException
	{
		super.drawTo(applet);
		
		_implementation = (p.g instanceof GLGraphics) ? new Fancy() : new Simple();
	}
	
	@Override
	public void process() throws PAVException
	{		
		_implementation.process();
	}
	
	@Override
	public String toString()
	{
		return "Wavering";
	}

	@Override
	public void dispose() { }
	
	/**
	 * The number of samples to use when interpolating between start and end value of the frame.
	 * 
	 * @param num Number of interpolated samples. Must be >= 0
	 */
	public void interpolate(int num)
	{
		_interpolate = num / 2;
	}
	
	/**
	 * Sets the relative base radius of the wavering.
	 * 
	 * @param rx Radius in x direction
	 * @param ry Radius in y direction
	 */
	public void setRadius(float rx, float ry)
	{
		_rx = rx;
		_ry = ry;
	}
	
	/**
	 * Sets the realtive maximum displacement of the ring coordinates.
	 * 
	 * @param dx Maximum displacement in x direction
	 * @param dy Maximum displacement in y direction
	 */
	public void setDisplacement(float dx, float dy)
	{
		_dx = dx;
		_dy = dy;
	}
	
	/**
	 * Automatically sets the radius and displacement based on the available display area.
	 */
	public void autosize()
	{
		setRadius(0.6f, 0.6f);
		setDisplacement(0.4f, 0.4f);
	}
	
	/**
	 * Sets the stroke weight to use when drawing.
	 * 
	 * @param weight The stroke weight. Must be > 0
	 */
	public void setStrokeWeight(float weight)
	{
		_strokeWeight = weight;
	}
	
	/**
	 * Whether to use the absolute values for coloring, i.e. whether -1 gets the same color as 1 or not.
	 * 
	 * @param absolute Whether to use absolute coloring
	 */
	public void colorAbsolute(boolean absolute)
	{
		if(absolute) {
			_colorAbsolute = true;
			cm.setRange(0, 1);
		}
		else {
			_colorAbsolute = false;
			cm.setRange(-1, 1);
		}
	}
	
	/**
	 * Returns the implementation used by this wavering.
	 * 
	 * @return The implementation
	 */
	public WaveringImpl getImplementation()
	{
		return _implementation;
	}
		
	private void _drawRing(PGraphics target)
	{
		target.beginDraw();
		target.noFill();
		target.beginShape();
		target.strokeWeight(_strokeWeight);
				
		float[] frame = Frame.samples();
		float[] area = getArea();
		int len = frame.length;
		int len1 = len - 1;
		float ox = (area[2] - area[0]) / 2;
		float oy = (area[3] - area[1]) / 2;
		
		float as = -(PApplet.PI * 1.5f);
		float ae = as + PApplet.TWO_PI;
		
		int j = (- len) + _interpolate;
		float diff = 0, ddiff = 0;
		
		if(_interpolate > 0) {
			diff = (frame[len - 1] - frame[0]) / 2;
			ddiff = diff / _interpolate;
		}
		
		float rx = _rx * ox;
		float ry = _ry * oy;
		float dx = _dx * ox;
		float dy = _dy * oy;
		
		for(int i = 0; i < len; i++, j++) {
			float v = frame[i];
			
			if(_interpolate > 0) {
				if(i < _interpolate) v += diff - i * ddiff;
				else if(j >= 0) v -= j * ddiff;
			}
			
			float angle = PApplet.map(i, 0, len1, as, ae);
			float x = ox + (rx + v * dx) * PApplet.cos(angle);
			float y = oy + (ry + v * dy) * PApplet.sin(angle);
			
			if(_colorAbsolute && v < 0)	target.stroke(cm.map(v * -1)); else target.stroke(cm.map(v));
						
			target.vertex(x, y);
		}
		
		target.endShape(PApplet.CLOSE);
		target.endDraw();
	}
	
	/**
	 * Wavering implementation.
	 * 
	 * @author christopher
	 */
	public abstract class WaveringImpl
	{
		/**
		 * Draws the implementation.
		 * 
		 * @throws PAVException On errors
		 */
		public abstract void process() throws PAVException;
		
		/**
		 * Disposes the imlementation.
		 */
		public abstract void dispose();
	}
	
	/**
	 * Simple implementation with software renderer support.
	 * 
	 * @author christopher
	 */
	public class Simple extends WaveringImpl
	{
		@Override
		public void process() throws PAVException
		{
			_drawRing(p.g);
		}

		@Override
		public void dispose() { }
	}
	
	/**
	 * Fancy implementation, requires GLGraphics renderer.
	 * 
	 * @author christopher
	 */
	public class Fancy extends WaveringImpl
	{
		private GLTexture _history, _bloomMap, _bloomMask, _bloom0, _bloom2, _bloom4, _bloom8, _bloom16, _out;
		private GLTextureFilter _fblur, _fcolorize, _freplace, _fblend4, _fextractBloom, _ftoneMap;
		private GLGraphicsOffScreen _off;
		
		/**
		 * Ctor.
		 */
		public Fancy() throws PAVException
		{
			_fblur = ShaderManager.getTextureFilter("Blur");
			_fcolorize = ShaderManager.getTextureFilter("Colorize");
			_freplace = ShaderManager.getTextureFilter("Replace");
			_fblend4 = ShaderManager.getTextureFilter("Blend4");
			_fextractBloom = ShaderManager.getTextureFilter("ExtractBloom");
			_ftoneMap = ShaderManager.getTextureFilter("ToneMap");
			
			setRingColor(0xFFFFFFFF);
			setDarkenFactor(0.006f);
			setBrightnessThreshold(0.3f);
			setBloomIntensity(0.5f);
			setTonemapExposure(1.6f);
			setTonemapMaxBrightness(0.9f);
		}
		
		/**
		 * Sets the color to use when drawing the ring.
		 * 
		 * @param color The ring color
		 */
		public void setRingColor(int color)
		{
			float r = p.red(color) / 255f;
			float g = p.green(color) / 255f;
			float b = p.green(color) / 255f;
			float a = p.alpha(color) / 255f;
			
			_fcolorize.setParameterValue("Color", new float[] { r, g, b, a });
		}
		
		/**
		 * Sets how fast old data disappears. Must be a small value (default is 0.005).
		 * 
		 * @param factor The darken factor
		 */
		public void setDarkenFactor(float factor)
		{
			_fblur.setParameterValue("DarkenFactor", factor);
		}
		
		/**
		 * Sets the brightness threshold to use when calculating the bloom map.
		 * 
		 * @param threshold The brightness threshold. Must be between 0 and 1
		 */
		public void setBrightnessThreshold(float threshold)
		{
			_fextractBloom.setParameterValue("bright_threshold", threshold);
		}
		
		/**
		 * Sets the intensity of the bloom effect.
		 * 
		 * @param intensity The bloom intensity. Must be >= 0
		 */
		public void setBloomIntensity(float intensity)
		{
			_ftoneMap.setParameterValue("bloom", intensity);
		}
		
		/**
		 * Sets the exposure to use for tone mapping.
		 * 
		 * @param exposure The exposure. Must be > 0
		 */
		public void setTonemapExposure(float exposure)
		{
			_ftoneMap.setParameterValue("exposure", exposure);
		}
		
		/**
		 * Sets the maximum brightness to use for tone mapping.
		 * 
		 * @param brightness The max brightness. Must be > 0
		 */
		public void setTonemapMaxBrightness(float brightness)
		{
			_ftoneMap.setParameterValue("bright", brightness);
		}
		
		@Override
		public void process() throws PAVException
		{
			float[] area = getArea();
			int width = (int) (area[2] - area[0]);
			int height = (int) (area[3] - area[1]);
			
			if(_off == null || _width != width || _height != height) {
				_width = width;
				_height = height;
				
				dispose();
				_init();
			}
			
			_off.beginDraw();
			_off.clear(0);
			_off.endDraw();
			
			_drawRing(_off);

			_fblur.apply(_history, _history);
			_fcolorize.apply(_off.getTexture(), _bloomMap);
			_freplace.apply(new GLTexture[] { _history, _off.getTexture() }, _history);

			_freplace.apply(new GLTexture[] { _history, _bloomMap }, _out);
			_fextractBloom.apply(_out, _bloom0);
			
			_bloom0.filter(_fblur, _bloom2);
			_bloom2.filter(_fblur, _bloom4);
			_bloom4.filter(_fblur, _bloom8);
			_bloom8.filter(_fblur, _bloom16);
			
			_fblend4.apply(new GLTexture[] { _bloom2, _bloom4, _bloom8, _bloom16}, new GLTexture[] { _bloomMask });
			_ftoneMap.apply(new GLTexture[] { _out, _bloomMask }, new GLTexture[] { _out });
			
			p.image(_out, area[0], area[1]);
		}
		
		@Override
		public void dispose()
		{
			if(_history != null) _history.delete();
			if(_bloomMap != null) _bloomMap.delete();
			if(_bloomMask != null) _bloomMask.delete();
			if(_bloom0 != null) _bloom0.delete();
			if(_bloom2 != null) _bloom2.delete();
			if(_bloom4 != null) _bloom4.delete();
			if(_bloom8 != null) _bloom8.delete();
			if(_bloom16 != null) _bloom16.delete();
			if(_out != null) _out.delete();
			if(_off != null) _off.dispose();
		}

		private void _init()
		{
			float[] area = getArea();
			_width = (int) (area[2] - area[0]);
			_height = (int) (area[3] - area[1]);
						
			_history = new GLTexture(p, _width, _height);
			_bloomMap = new GLTexture(p, _width, _height);
			_bloomMask = new GLTexture(p, _width, _height);
			_bloom0 = new GLTexture(p, _width, _height);
		    _bloom2 = new GLTexture(p, _width / 2, _height / 2);
		    _bloom4 = new GLTexture(p, _width / 4, _height / 4);
		    _bloom8 = new GLTexture(p, _width / 8, _height / 8);
		    _bloom16 = new GLTexture(p, _width / 16, _height / 16);
			_out = new GLTexture(p, _width, _height);
			
			_off = new GLGraphicsOffScreen(p, _width, _height);
		}
	}
}
