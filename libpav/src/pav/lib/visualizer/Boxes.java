
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

import java.io.Serializable;
import pav.lib.PAVException;
import pav.lib.Util;
import pav.lib.frame.Frame;
import pav.lib.frame.TransformResult;
import processing.core.PApplet;
import codeanticode.glgraphics.GLGraphics;
import codeanticode.glgraphics.GLGraphicsOffScreen;

/**
 * Draws a 3D grid of boxes with sizes linked to intensities of frequency bands.
 * 
 * As of the current version this visualizer only works with the GLGraphics renderer.
 * 
 * @author christopher
 */
public class Boxes extends VisualizerAbstract
{
	private static final long serialVersionUID = -2867609623075114719L;
	
	/**
	 * Use continuous color interpolation. Every box will have a different color.
	 */
	public static final int COLOR_INTERPOLATION_CONTINUOUS = 1;
	
	/**
	 * Interpolate colors according to rows. Every row will have a different color.
	 */
	public static final int COLOR_INTERPOLATION_ROW = 2;
	
	/**
	 * Use linear linking between boxes and frequency ranges. Boxes linked to low
	 * frequencies will be positioned at the far distance with default camera settings.
	 */
	public static final int LINK_LINEAR = 1;
		
	private final Box[] _boxes;
	private final int _numRows, _numCols;
	private final float _gridSizeZ;
	private final int[] _intensityMap, _colorMap;
	
	private float[] _filter;
	private int _maxHeight, _edgeColor, _quantizationSteps ;
	private float _rotateAngle, _rotateSpeed;
	private transient float _vMax;
	private transient float[] _intensities;
	private transient GLGraphicsOffScreen _buffer;

	/**
	 * Ctor.
	 */
	public Boxes()
	{
		this(64, 30, 3);
	}
	
	/**
	 * Ctor.
	 * 
	 * @param num The number of boxes to create. Must be > 0
	 * @param size The length of the box edges. Must be > 0
	 * @param spacing The spacing between boxes
	 */
	public Boxes(int num, int size, int spacing)
	{
		this(num, size, spacing, (int) Math.sqrt(num));
	}
	
	/**
	 * Ctor.
	 * 
	 * @param num The number of boxes to create. Must be > 0
	 * @param size The length of the box edges. Must be > 0
	 * @param spacing The spacing between boxes
	 * @param rows The number of box rows to create. Must be > 0
	 */
	public Boxes(int num, int size, int spacing, int rows)
	{
		super();
	
		_boxes = new Box[num];
		_intensities = new float[num];
		_colorMap = new int[num];
		_intensityMap = new int[num];
		
		int i = 0;
		int dx = size + spacing;
		int dz = size + spacing;
		
		_numRows = rows;
		_numCols = (int) Math.ceil(num / (float) rows);
		_gridSizeZ = _numCols * size + (_numCols - 1) * spacing;
		
		float offsetX = ((_numRows - 1) * size + (_numRows - 1) * spacing) / 2f;
		float offsetZ = ((_numCols - 1) * size + (_numCols - 1) * spacing) / 2f;
		
		for(int x = 0; x < _numRows; x++) {
			int numMissing = (x == _numRows - 1) ? _numRows * _numCols - num : 0;
			
			for(int z = 0; z < _numCols; z++) {
				if(i == num) {
					break;
				}
				
				float offsetMissing = (x % 2 == 0) ? dz * numMissing : 0;
				
				_boxes[i] = new Box(i, x * dx - offsetX, 0, z * dz - offsetZ + offsetMissing, size, size);
				
				i++;
			}
		}
				
		setMaxHeight(125);
		setLinkMode(LINK_LINEAR);
		setEdgeColor(0xFFCCCCCC);
		setColor(new float[] { 0, 0.2f, 0.6f, 1 }, new int[] { 0xFF0000FF, 0xFF00FF00, 0xFFFFFF00, 0xFFFF0000 }, PApplet.RGB);
	}
	
	/**
	 * Sets the camera to use. See PApplet.camera() for more infos.
	 * 
	 * Note that the camera is reset every time the display size changes, therefore
	 * calls to this method before the visualization is drawn for the first time have no effect.
	 * 
	 * @param eyeX The eye x position
	 * @param eyeY The eye y position
	 * @param eyeZ The eye z position
	 * @param centerX The center x position
	 * @param centerY The center y position
	 * @param centerZ The center z position
	 * @param upX The up x position
	 * @param upY The up y position
	 * @param upZ The up z position
	 */
	public void setCamera(float eyeX, float eyeY, float eyeZ, float centerX, float centerY, float centerZ, float upX, float upY, float upZ)
	{
		if(_buffer == null) {
			return;
		}
		
		_buffer.beginDraw();
		_buffer.camera(eyeX, eyeY, eyeZ, centerX, centerY, centerZ, upX, upY, upZ);
		_buffer.endDraw();
	}
	
	/**
	 * Resets the camera position.
	 * 
	 * The eyeX position is set to the width of the grid. eyeY is set to h/-5 where h is the
	 * display height. All other position are set to 0 (see setCamera()), with upY being 1.
	 * These settings are fitting only if the grid is nearly rectangular (which it is by default).
	 */
	public void resetCamera()
	{
		if(_buffer == null) {
			return;
		}
		
		setCamera(_gridSizeZ, _buffer.height / -5, 0, 0, 0, 0, 0, 1, 0);
	}
	
	/**
	 * Starts or stops rotating. The speed is set via speed, which is a value in radians
	 * that specifies how the angle will change every time process() is called. A fitting value
	 * is Pi/360. If speed is set to 0 the rotation stops.
	 * 
	 * @param speed The rotation speed
	 */
	public void rotate(float speed)
	{
		_rotateSpeed = speed;
	}
	
	/**
	 * Sets the fill color of the boxes.
	 * 
	 * @param color The color to use
	 */
	@Override
	public void setColor(int color)
	{
		setColor(new float[] { 0, 1 }, new int[] { color, color }, PApplet.RGB, COLOR_INTERPOLATION_CONTINUOUS);
	}
	
	/**
	 * Sets the fill color of the boxes using continuous interpolation mode.
	 * 
	 * @param a The color to start from
	 * @param b The color to interpolate to
	 * @param mode The color mode to use. Must bei either PApplet.RGB or PApplet.HSB
	 */
	@Override
	public void setColor(int a, int b, int mode)
	{
		setColor(a, b, mode, COLOR_INTERPOLATION_CONTINUOUS);
	}
	
	/**
	 * Sets the fill color of the boxes.
	 * 
	 * @param a The color to start from
	 * @param b The color to interpolate to
	 * @param mode The color mode to use. Must bei either PApplet.RGB or PApplet.HSB
	 * @param interpolation The interpolation used. See COLOR_INTERPOLATION constants of this class. Must be valid
	 */
	public void setColor(int a, int b, int mode, int interpolation)
	{
		setColor(new float[] { 0, 1 }, new int[] { a, b }, mode, interpolation);
	}
	
	/**
	 * Sets the fill color of the boxes.
	 * 
	 * @param thresholds The relative thresholds to use. Values must be between 0 and 1 and sorted. The first element must be 0, the last 1. Must be of same length as colors
	 * @param colors The The colors to use. Must be of same length as thresholds
	 * @param mode The color mode to use. Must bei either PApplet.RGB or PApplet.HSB
	 */
	@Override
	public void setColor(float[] thresholds, int[] colors, int mode)
	{
		setColor(thresholds, colors, mode, COLOR_INTERPOLATION_CONTINUOUS);
	}
	
	/**
	 * Sets the fill color of the boxes.
	 * 
	 * @param thresholds The relative thresholds to use. Values must be between 0 and 1 and sorted. The first element must be 0, the last 1. Must be of same length as colors
	 * @param colors The The colors to use. Must be of same length as thresholds
	 * @param mode The color mode to use. Must bei either PApplet.RGB or PApplet.HSB
	 * @param interpolation The interpolation used. See COLOR_INTERPOLATION constants of this class. Must be valid
	 */
	public void setColor(float[] thresholds, int[] colors, int mode, int interpolation)
	{
		if(_colorMap == null) {
			return; // can occur only on initialization
		}
		
		int numColors = _colorMap.length;
		super.setColor(thresholds, colors, mode);
		
		switch(interpolation) {
			case COLOR_INTERPOLATION_CONTINUOUS :
				cm.setRange(0, numColors - 1);
				
				for(int i = 0; i < _numRows; i++) {
					int offset = (i == _numRows - 1) ? _numRows * _numCols - _boxes.length : 0;

					for(int j = 0; j < _numCols - offset; j++) {
						int index = _numCols * i + j;
						
						if(i % 2 == 0) {
							if(j <= _numCols / 2) {
								int swap = _numCols * i + _numCols - j - offset - 1;

								_colorMap[index] = cm.map(swap);
								_colorMap[swap] = cm.map(index);
							}
						}
						else {
							_colorMap[index] = cm.map(index);
						}
					}
				}
				
				break;
			case COLOR_INTERPOLATION_ROW :
				cm.setRange(0, _numCols - 1);
				
				for(int i = 0; i < numColors; i++) {
					_colorMap[i] = cm.map(i / _numRows);
				}
				
				break;
		}
	}
	
	/**
	 * Sets the color of the box edges.
	 * 
	 * @param color Box edge color
	 */
	public void setEdgeColor(int color)
	{
		_edgeColor = color;
	}
	
	/**
	 * Sets the maximum height of the boxes.
	 * 
	 * @param height The maximum box height
	 */
	public void setMaxHeight(int height)
	{
		_maxHeight = height;
	}
	
	/**
	 * Sets the number of steps to use for quantization of the spectrum values or disables quantization.
	 * 
	 * @param steps The number of quantization steps. Set to <= 0 to disable quantization
	 */
	public void quantize(int steps)
	{
		_quantizationSteps = (steps < 0) ? 0 : steps;
		_vMax = 0;
	}
	
	/**
	 * Filters the calculated spectrum intensities before displaying them.
	 * The process is simply carried out by multiplying the i-th spectrum intensity
	 * with the i-th value in the filter. Thus, the filter length must not be longer
	 * than the number of calculated bands (that is the number of boxes), but it can be lower.
	 * The link mode (see setLinkMode()) determines what box will be modified by what filter value.
	 * 
	 * @param filter The filter to use or null to disable filtering. Filter length must not be larger than the number of boxes
	 */
	public void filter(float[] filter)
	{
		_filter = filter;
	}
	
	/**
	 * Sets the link mode. See LINK_ constants of this class.
	 * 
	 * Currently only linear linking mode is supported.
	 * 
	 * @param mode The link mode to use. Must be valid
	 */
	public void setLinkMode(int mode)
	{
		switch(mode) {
			case LINK_LINEAR :			
				for(int i = 0; i < _numRows; i++) {
					int offset = (i == _numRows - 1) ? _numRows * _numCols - _boxes.length : 0;

					for(int j = 0; j < _numCols - offset; j++) {
						int index = _numCols * i + j;
						
						if(i % 2 == 0) {
							if(j <= _numCols / 2) {
								int swap = _numCols * i + _numCols - j - offset - 1;

								_intensityMap[index] = swap;
								_intensityMap[swap] = index;
							}
						}
						else {
							_intensityMap[index] = index;
						}
					}
				}
			break;
		}
	}

	@Override
	public void process() throws PAVException
	{
		float[] area = getArea();
		int width = (int) (area[2] - area[0]);
		int height = (int) (area[3] - area[1]);
		
		if(_buffer == null || _buffer.width != width || _buffer.height != height) {
			if(_buffer != null) {
				_buffer.dispose();
			}
			
			if(! (p.g instanceof GLGraphics)) {
				throw new PAVException("Boxes visualizer requires GLGraphics renderer!");
			}
			
			_buffer = new GLGraphicsOffScreen(p, width, height, true);
			resetCamera();
		}
		
		if(_rotateSpeed != 0) {
			_rotateAngle += _rotateSpeed;
			setCamera(PApplet.cos(_rotateAngle) * _gridSizeZ, p.height / -5, PApplet.sin(_rotateAngle) * _gridSizeZ, 0, 0, 0, 0, 1, 0);
		}
		
		TransformResult spectrum = Frame.Transform.melSpectrum(_boxes.length);
		_intensities = spectrum.frame();
		float vMax = spectrum.max();

		if(_filter != null) {
			int len = _filter.length;
			_intensities = (float[]) _intensities.clone();	// changes to TransformResults are not allowed
			
			for(int i = 0; i < len; i++) {
				_intensities[i] = _intensities[i] * _filter[i];
				
				if(_intensities[i] > vMax) {
					vMax = _intensities[i];
				}
			}
		}
		
		if(vMax > _vMax) {
			_vMax = vMax;
		}
		
		if(_quantizationSteps > 0) {
			_intensities = Util.quantize(_intensities, _quantizationSteps, 0, _vMax);
		}
		
		_buffer.beginDraw();
		_buffer.clear(0);
		_buffer.strokeWeight(1);
		_buffer.stroke(_edgeColor);
		
		for(Box b : _boxes) {
			b.draw();
		}
		
		_buffer.endDraw();
		
		p.image(_buffer.getTexture(), area[0], area[1]);
	}
		
	/**
	 * A box.
	 * 
	 * @author christopher
	 */
	private class Box implements Serializable
	{
		private static final long serialVersionUID = 546482976109598332L;
		
		private float _x, _y, _z;
		private float _dx, _dz;
		private final int _id;
		
		/**
		 * Ctor.
		 * 
		 * @param id The id of this box. Must be unique, > -1 and < _boxes.length
		 * @param x The x position
		 * @param y The y position
		 * @param z The z position
		 * @param sx The x size
		 * @param sz The z size
		 */
		public Box(int id, float x, float y, float z, float sx, float sz)
		{
			_id = id;
			size(sx, sz);
			translate(x, y, z);
		}
		
		/**
		 * Sets the box size.
		 * 
		 * @param x The x size
		 * @param z The z size
		 */
		public void size(float x, float z)
		{
			_dx = x;
			_dz = z;
		}
		
		/**
		 * Sets the position of the box.
		 * 
		 * @param x The x position
		 * @param y The y position
		 * @param z The z position
		 */
		public void translate(float x, float y, float z)
		{
			_x = x;
			_y = y;
			_z = z;
		}
		
		/**
		 * Draws the box. Buffer drawing must be enabled.
		 */
		public void draw()
		{
			float min = (_quantizationSteps > 0) ? 1 : 0;
			float max = (_quantizationSteps > 0) ? _quantizationSteps : _vMax;
			float height = PApplet.map(_intensities[_intensityMap[_id]], min, max, 1, _maxHeight);
			
			_buffer.fill(_colorMap[_id]);
			_buffer.pushMatrix();
			_buffer.translate(_x, _y - height / 2, _z);
			_buffer.box(_dx, height, _dz);
			_buffer.popMatrix();
		}
	}
}
