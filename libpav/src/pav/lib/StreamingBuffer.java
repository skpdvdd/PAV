
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

import processing.core.PApplet;
import processing.core.PImage;
import processing.opengl.PGraphicsOpenGL;
import codeanticode.glgraphics.GLGraphics;
import codeanticode.glgraphics.GLGraphicsOffScreen;
import codeanticode.glgraphics.GLTexture;

/**
 * A streaming image buffer, useful in some visualizations such as spectogram.
 * 
 * This Uses GLGraphics if available. The Processing version supports alpha channel
 * transparency but is rather slow. The GLGraphics version is optimized for speed.
 * It does not support transparency. Also, the GLGraphics implementation currently ignores
 * the width argument of the draw method for implementation reasons. This does however
 * not affect the result if the buffer is drawn to occupy the full area of the host PApplet.
 * 
 * @author christopher
 */
public class StreamingBuffer
{
	private final int _width;
	private final int _height;
	private final PApplet _p;
	private final StreamingBufferImpl _impl;
	
	/**
	 * Ctor.
	 * 
	 * @param p The PApplet this buffer should draw itself to. Must not be null
	 * @param width The width of the buffer. Must be positive
	 * @param height The height of the buffer. Must be positive
	 */
	public StreamingBuffer(PApplet p, int width, int height)
	{
		_p = p;
		_width = width;
		_height = height;
		_impl = (p.g instanceof GLGraphics) ? new StreamingBufferGLGraphics() : new StreamingBufferPImage();
	}
	
	/**
	 * Gets the buffer width
	 * 
	 * @return Buffer width
	 */
	public int getWidth()
	{
		return  _width;
	}
	
	/**
	 * Gets the buffer height
	 * 
	 * @return Buffer height
	 */
	public int getHeight()
	{
		return _height;
	}
	
	/**
	 * Adds a new data sample to the buffer. This is equal to add(data, true).
	 * 
	 * @param data The data sample to add. The array length must be equal to the buffer height
	 * @throws PAVException If the array length is not equal to the buffer height
	 */
	public void add(int[] data) throws PAVException
	{
		add(data, true);
	}
	
	/**
	 * Adds a new data sample to the buffer.
	 * 
	 * @param data The data sample to add. The array length must be equal to the buffer height
	 * @param startOnTop Whether to fill the buffer column from top to bottom or vice versa
	 * @throws PAVException If the array length is not equal to the buffer height
	 */
	public void add(int[] data, boolean startOnTop) throws PAVException
	{
		if(_height != data.length) {
			throw new PAVException("Length of the data argument does not match the buffer height.");
		}
		
		_impl.add(data, startOnTop);
	}
	
	/**
	 * Draws this buffer to a given area of the host PApplet.
	 * 
	 * @param xStart The first x-coordinate of the display area
	 * @param yStart The first y-coordinate of the display area
	 * @param width The width of the display area. Must be > 0. Ignored under GLGraphics
	 * @param height The height of the display area. Must be > 0
	 */
	public void draw(int xStart, int yStart, int width, int height)
	{
		_impl.draw(xStart, yStart, width, height);
	}
	
	/**
	 * Disposes this buffer.
	 * 
	 * Subsequent calls to other methods of this class might throw an exception.
	 */
	public void dispose()
	{
		_impl.dispose();
	}
	
	/**
	 * Buffer implementation.
	 * 
	 * @author christopher
	 */
	private abstract class StreamingBufferImpl
	{
		/**
		 * Adds a new data sample to the buffer.
		 * 
		 * @param data The data sample to add. The array length must be equal to the buffer height
		 * @param startOnTop Whether to fill the buffer column from top to bottom or vice versa
		 */
		public abstract void add(int[] data, boolean startOnTop);
		
		/**
		 * Draws this buffer to a given area of the host PApplet.
		 * 
		 * @param xStart The first x-coordinate of the display area
		 * @param yStart The first y-coordinate of the display area
		 * @param width The width of the display area. Must be > 0
		 * @param height The height of the display area. Must be > 0
		 */
		public abstract void draw(int xStart, int yStart, int width, int height);
		
		/**
		 * Disposes this buffer.
		 * 
		 * Subsequent calls to other methods of this class might throw an exception.
		 */
		public abstract void dispose();
	}
	
	/**
	 * Buffer implementation using a single PImage object.
	 * 
	 * This implementation uses only a single PImage object and
	 * supports alpha channel transparency. It is however rather slow.
	 * 
	 * @author christopher
	 */
	private class StreamingBufferPImage extends StreamingBufferImpl
	{
		private int _counter, _x;
		private boolean _switch;
		private final PImage _buffer;
		
		/**
		 * Ctor.
		 */
		public StreamingBufferPImage()
		{
			_buffer = new PImage(_width, _height, PApplet.ARGB);
		}
		
		@Override
		public void add(int[] data, boolean startOnTop)
		{
			int len = data.length;
			int k = _counter % _width;
			
			_x = _width - k - 1;
			
			_buffer.loadPixels();

			for(int i = 0; i < len; i++) {
				if(startOnTop) {
					_buffer.pixels[i * _width + _x] = data[i];
				}
				else {
					_buffer.pixels[(len - i - 1) * _width + _x] = data[i];
				}
			}
			
			_buffer.updatePixels();
			
			_counter++;
			
			if(! _switch && _counter == _width) {
				_switch = true;
			}
		}

		@Override
		public void draw(int xStart, int yStart, int width, int height)
		{
			_p.blend(_buffer, _x, 0, _width - _x, _height, xStart, yStart, width - _x, height, PApplet.BLEND);
			
			if(_switch) {
				_p.blend(_buffer, 0, 0, _x, _height, xStart + width - _x, yStart, _x, height, PApplet.BLEND);
			}
		}

		@Override
		public void dispose() { }
	}
	
	/**
	 * Buffer implementation using GLGraphics.
	 * 
	 * This implementation is very fast, but it does not support alpha
	 * channel transparency for the sake of speed. Also, the width argument
	 * of the draw() method is currently ignored.
	 * 
	 * @author christopher
	 */
	private class StreamingBufferGLGraphics extends StreamingBufferImpl
	{
		private int _counter, _x, _numBufferSwaps;
		private final GLTexture _column;
		private final GLGraphicsOffScreen _buffer1, _buffer2;
		
		/**
		 * Ctor.
		 */
		public StreamingBufferGLGraphics()
		{
			_buffer1 = new GLGraphicsOffScreen(_p, _width, _height);
			_buffer1.beginDraw();
			_buffer1.clear(0, 0);
			_buffer1.noStroke();
			_buffer1.endDraw();
			
			_buffer2 = new GLGraphicsOffScreen(_p, _width, _height);
			_buffer2.beginDraw();
			_buffer2.clear(0, 0);
			_buffer2.noStroke();
			_buffer2.endDraw();
			
			_column = new GLTexture(_p, 1, _height);
		}
		
		@Override
		public void add(int[] data, boolean startOnTop)
		{
			int k = _counter % _width;
			GLGraphicsOffScreen active, passive;
			
			_x = _width - k - 1;	
			
			if(_numBufferSwaps % 2 == 0) {
				active = _buffer1;
				passive = _buffer2;
			}
			else {
				active = _buffer2;
				passive = _buffer1;
			}
			
			int[] in;
			
			if(startOnTop) {
				in = data;
			}
			else {
				int len = data.length;
				int lenm1 = len - 1;
				int max = len / 2;
				in = new int[len];
				
				for(int i = 0; i < max; i++) {
					in[i] = data[lenm1 - i];
					in[lenm1 - i] = data[i];
				}
				
				if(len % 2 == 1) {
					in[max] = data[max];
				}
			}
			
			_column.putBuffer(in, PApplet.RGB, GLTexture.TEX_BYTE);
					
			active.beginDraw();
			active.image(_column, _x, 0);
			active.endDraw();
			
			if(k == 0 && _counter != 0) {
				passive.beginDraw();
				passive.clear(0, 0);
				passive.endDraw();
				
				_numBufferSwaps++;
			}
			
			_counter++;
		}

		/**
		 * Draws this buffer to a given area of the host PApplet.
		 * 
		 * @param xStart The first x-coordinate of the display area
		 * @param yStart The first y-coordinate of the display area
		 * @param width The width of the display area. Must be > 0. Ignored in this implementation
		 * @param height The height of the display area. Must be > 0
		 */
		@Override
		public void draw(int xStart, int yStart, int width, int height)
		{
			PGraphicsOpenGL p = (PGraphicsOpenGL) _p.g;
			GLGraphicsOffScreen active, passive;
			
			if(_numBufferSwaps % 2 == 0) {
				active = _buffer1;
				passive = _buffer2;
			}
			else {
				active = _buffer2;
				passive = _buffer1;
			}
			
			p.image(active.getTexture(), xStart -_x, yStart, width, height);
			p.image(passive.getTexture(), xStart -_x + _width - 1, yStart, width, height);
		}

		@Override
		public void dispose()
		{
			_buffer1.dispose();
			_buffer2.dispose();
			_column.delete();
		}
	}
}
