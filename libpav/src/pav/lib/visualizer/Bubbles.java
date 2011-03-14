
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

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Random;
import pav.lib.PAVException;
import pav.lib.ShaderManager;
import pav.lib.frame.Frame;
import processing.core.PApplet;
import codeanticode.glgraphics.GLGraphics;
import codeanticode.glgraphics.GLGraphicsOffScreen;
import codeanticode.glgraphics.GLTexture;
import codeanticode.glgraphics.GLTextureFilter;

/**
 * Generates bubbles based on sound. Requires GLGraphics render mode.
 * 
 * @author christopher
 */
public class Bubbles extends VisualizerAbstract
{	
	private static final long serialVersionUID = 4343458995928287150L;
	
	private Generator _generator;
	private GLGraphicsOffScreen _active, _done;
	private GLTexture _history, _age, _temp, _temp2;
	private GLTextureFilter _blur, _blend, _ageUpdate, _bloom;
	
	private final Random _random;
	private final LinkedList<Bubble> _bubbles, _finished;
	
	private boolean _useBloom;
	private final LinkedList<Integer> _spawnHistory;
	private int _spawnSum, _spawnHistorySize, _width, _height;
	private float _spawnRateMin, _spawnRateMax, _rMin, _rMax, _darkenFactor, _spawnAvg;
	
	/**
	 * Ctor.
	 */
	public Bubbles()
	{
		_spawnAvg = 1;
		_spawnHistorySize = 130;
		_spawnHistory = new LinkedList<Integer>();
		
		_random = new Random();
		_bubbles = new LinkedList<Bubble>();
		_finished = new LinkedList<Bubble>();
		_generator = new Intensity();
		
		useBloom(true);
		setDarkenFactor(12);
		setBubbleSize(0.02f, 0.06f);
		setSpawnRate(0.3f, 1.5f);
		setColor(new float[] { 0, 0.33f, 0.66f, 1 }, new int[] { 0xFF00FF00, 0xFFFFFF00, 0xFFFF0000, 0xFFFF00FF }, PApplet.RGB);
	}
	
	/**
	 * Sets the darken factor. This factor will influence how fast drawn bubbles will disappear.
	 * 
	 * Fitting values are based on many variables, mainly on the spawn rate (see setSpawnRateTargets()).
	 * Default is 12.
	 * 
	 * @param factor The darken factor. Must be positive
	 */
	public void setDarkenFactor(float factor)
	{
		_darkenFactor = factor;
	}
	
	/**
	 * Sets the minimum and maximum size of the bubbles.
	 * The values are relative, f.i. 0.2 means 0.2 times display area width.
	 * 
	 * @param min The minimum bubble size. Must be positive
	 * @param max The maximum bubble size. Must be > min
	 */
	public void setBubbleSize(float min, float max)
	{
		_rMin = min;
		_rMax = max;
	}
	
	/**
	 * Whether to use a bloom postprocessing filter.
	 * 
	 * @param bloom Whether to use bloom postprocessing
	 */
	public void useBloom(boolean bloom)
	{
		_useBloom = bloom;
	}
	
	/**
	 * Sets the spawn rate. The visualization will try to spawn
	 * at least min bubbles and at the most max bubbles per call to process().
	 * 
	 * @param min The min number of bubbles to spawn. Must be > 0
	 * @param max The max number of bubbles to spawn. Must be > min
	 */
	public void setSpawnRate(float min, float max)
	{
		_spawnRateMin = min;
		_spawnRateMax = max;
	}
	
	@Override
	public void drawTo(PApplet applet) throws PAVException
	{		
		if(! (applet.g instanceof GLGraphics)) {
			throw new PAVException("Bubbles visualizer requires GLGraphics renderer.");
		}
		
		super.drawTo(applet);
		
		dispose();
				
		_done = null;
		_active = null;
		
		_blur = ShaderManager.getTextureFilter("BubblesBlur");
		
		_blend = ShaderManager.getTextureFilter("BubblesBlend");
		_blend.setParameterValue("EmphBase", 0.1f);
		_blend.setParameterValue("EmphBlend", 0.15f);
				
		_ageUpdate = ShaderManager.getTextureFilter("AgeUpdate");
		_ageUpdate.setParameterValue("Add", 0.05f);
		
		_bloom = ShaderManager.getTextureFilter("BubblesBloom");
		_bloom.setParameterValue("T1", 1.5f);
		_bloom.setParameterValue("T2", 2.5f);
		_bloom.setParameterValue("Intensity", 0.6f);
		_bloom.setParameterValue("LumaCoeffs", new float[] { 0.5f, 0.5f, 0.5f, 1f });
	}
	
	@Override
	public void process() throws PAVException
	{
		float[] area = getArea();
		int width = (int) (area[2] - area[0]);
		int height = (int) (area[3] - area[1]);
		
		if(_active == null || _done == null || _width != width || _height != height) {
			_width = width;
			_height = height;
			
			_init();
		}
		
		int num = _generator.generate();
		
		_spawnHistory.add(num);
		_spawnSum += num;
		
		if(_spawnHistory.size() > _spawnHistorySize) {
			_spawnSum -= _spawnHistory.removeFirst();
		}
		
		_spawnAvg = _spawnSum / (float) _spawnHistory.size();
		
		_active.beginDraw();	
		_active.clear(0, 0);
		_active.endDraw();
		
		_finished.clear();
		
		for(Bubble b : _bubbles) {
			if(b.draw()) {
				_finished.add(b);
			}
		}
		
		for(Bubble b : _finished) {
			_bubbles.remove(b);
		}
		
		_ageUpdate.apply(new GLTexture[] { _age, _done.getTexture() }, _age);
		
		if(! _finished.isEmpty()) {
			GLTexture d = _done.getTexture();
			_blend.apply(new GLTexture[] { _history, d }, _history);

			_done.beginDraw();	
			_done.clear(0, 0);
			_done.endDraw();
		}
		
		_blur.setParameterValue("DarkenFactor", PApplet.map(_spawnAvg, 0, _darkenFactor, 0, 0.025f) + 0.0002f);
		_blur.apply(new GLTexture[] { _history, _age }, _history);
		
		GLTexture a = _active.getTexture();
		_blend.apply(new GLTexture[] { _history, a }, _temp);
		
		if(_useBloom) {
			_temp2.clear(0, 0);
			_temp.filter(_bloom, _temp2);
			p.image(_temp2, area[0], area[1]);
		}
		else {
			p.image(_temp, area[0], area[1]);
		}
	}
	
	@Override
	public String toString()
	{
		return "Bubbles";
	}
	
	private float _cx()
	{
		float f = (float) _random.nextGaussian();

		return PApplet.map(f, -1.25f, 1.25f, 0, _width);
	}
	
	private float _cy()
	{
		float f = (float) _random.nextGaussian();
		
		return PApplet.map(f, -1.8f, 1.8f, 0, _height);
	}
	
	private void _init() throws PAVException
	{
		if(! (p.g instanceof GLGraphics)) {
			throw new PAVException("Bubbles visualizer requires GLGraphics renderer.");
		}
		
		if(_active != null) _active.dispose();
		if(_done != null) _done.dispose();
		
		_active = new GLGraphicsOffScreen(p, _width, _height);
		_active.beginDraw();
		_active.ellipseMode(PApplet.RADIUS);
		_active.endDraw();
		
		_done = new GLGraphicsOffScreen(p, _width, _height);
		_done.beginDraw();
		_done.ellipseMode(PApplet.RADIUS);
		_done.endDraw();
		
		_history = new GLTexture(p, _width, _height);
		_history.clear(0, 0);
		
		_age = new GLTexture(p, _width, _height);
		_age.clear(0);
		
		_temp = new GLTexture(p, _width, _height);
		_temp.clear(0, 0);
		
		_temp2 = new GLTexture(p, _width, _height);
		_temp2.clear(0, 0);
		
		cm.setRange(0, _width);
	}
	
	/**
	 * A bubble generator.
	 * 
	 * @author christopher
	 */
	private abstract class Generator
	{
		/**
		 * Generates bubbles.
		 * 
		 * @return The number of generated bubbles
		 */
		public abstract int generate();
	}
	
	/**
	 * Generates bubbles based on the intensity of the playing sound.
	 * 
	 * @author christopher
	 */
	private class Intensity extends Generator
	{
		private int _seq;
		private float _align;
		private final int[] _lutMin, _lutMax;
		private float _rmsMin, _rmsMax, _srtMin, _srtMax;
		
		/**
		 * Ctor.
		 */
		public Intensity()
		{
			_lutMin = new int[10];
			_lutMax = new int[10];
			_rmsMin = Float.MAX_VALUE;
			_rmsMax = Float.MIN_VALUE;
			
			setAlignmentFactor(0.0002f);
		}
		
		/**
		 * Sets the alignment factor. Must be a small value, default is 0.0002.
		 * 
		 * @param align The alignment factor. Must be > 0
		 */
		public void setAlignmentFactor(float align)
		{
			_align = align;
		}
		
		@Override
		public int generate()
		{
			if(_srtMin != _spawnRateMin || _srtMax != _spawnRateMax) {
				_updateLuts();
			}
			
			float rms = Frame.Descriptor.rms();
			
			if(rms < 0.001) return 0;
			if(rms < _rmsMin) _rmsMin = rms; else _rmsMin += _align;
			if(rms > _rmsMax) _rmsMax = rms; else _rmsMax -= _align;
			
			int idx = _seq % 10;
			float t = PApplet.constrain(PApplet.map(rms, _rmsMin, _rmsMax, 0, 1), 0, 1);
			float spawn = PApplet.map(t, 0, 1, _lutMin[idx], _lutMax[idx]);
			int num = (int) spawn;
			float rem = spawn - num;
			
			if(_random.nextFloat() < rem) num++;

			for(int i = 0; i < num; i++) {
				float r = PApplet.map(t, 0, 1, _width * _rMin, _width * _rMax);
				float x = _cx();
				float y = _cy();
				
				float co = ((float) _random.nextGaussian()) * _width / 8f;
				
				int c = cm.map(x + co);
				int cr = c >> 16 & 0xFF;
				int cg = c >> 8 & 0xFF;
				int cb = c & 0xFF;
				int ca = (int) PApplet.map(t, 0, 1, 100, 175);

				_bubbles.add(new Bubble(x, y, r, ca, cr, cg, cb, t + 0.75f, 6));
			}
			
			_seq++;
			
			return num;
		}
		
		private void _updateLuts()
		{
			_srtMin = _spawnRateMin;
			_srtMax = _spawnRateMax;
			
			int numMin = Math.round(_srtMin * 10);
			int numMax = Math.round(_srtMax * 10);
			
			_lutFill(_lutMin, numMin);
			_lutFill(_lutMax, numMax);
		}
		
		private void _lutFill(int[] lut, int num)
		{
			int min = num / 10;
			int rem = num - min * 10;
			
			Arrays.fill(lut, min);
			
			if(rem == 0) {
				return;
			}
			
			int step = 10 / rem;
			int p = 0;
			
			while(p < rem) {
				lut[p * step] += 1;
				p++;
			}
		}
	}
	
	/**
	 * A spawning bubble.
	 * 
	 * @author christopher
	 */
	private class Bubble
	{
		private int _st;
		private final float _cx, _cy, _r, _sw;
		private final int _c, _ca, _cr, _cg, _cb, _sd;
		
		/**
		 * Ctor.
		 * 
		 * @param cx The center x coordinate of the bubble
		 * @param cy The center y coordinate of the bubble
		 * @param r The radius of the bubble
		 * @param ca The alpha of the bubble color
		 * @param cr The red part of the bubble color
		 * @param cg The green part of the bubble color
		 * @param sw The stroke weight of the bubble border
		 * @param cb The blue part of the bubble color
		 * @param sd The spawn duration in frames. Must be > 0
		 */
		public Bubble(float cx, float cy, float r, int ca, int cr, int cg, int cb, float sw, int sd)
		{
			_cx = cx;
			_cy = cy;
			_r = r;
			_ca = ca;
			_cr = cr;
			_cg = cg;
			_cb = cb;
			_sw = sw;
			_sd = sd;

			_c = p.color(cr, cg, cb, ca);
		}
		
		/**
		 * Draws the bubble and returns true if the spawn process has been completed.
		 * If called called with an already completely spawned bubble, this method does nothing and returns true.
		 * 
		 * @return Whether the bubble is spawned completely
		 */
		public boolean draw()
		{
			_st++;
			
			if(_st > _sd) {
				return true;
			}
			
			if(_st == _sd) {
				_done.beginDraw();
				_done.fill(_c);
				_done.strokeWeight(_sw);
				_done.stroke(p.color(_cr, _cg, _cb, Math.min(_ca + 100, 255)));
				_done.ellipse(_cx, _cy, _r, _r);
				_done.endDraw();
				
				return true;
			}
			else {
				float r = PApplet.map(_st, 1, _sd, _r / 1.25f, _r);
				float ca = PApplet.map(_st, 1, _sd, _ca + 75, Math.min(_ca + 100, 255));
				
				_active.beginDraw();;
				_active.fill(_c);
				_active.strokeWeight(_sw);
				_active.stroke(p.color(_cr, _cg, _cb, ca));
				_active.ellipse(_cx, _cy, r, r);
				_active.endDraw();
				
				return false;
			}
		}
	}

	@Override
	public void dispose()
	{
		if(_active != null) _active.dispose();
		if(_done != null) _active.dispose();
		if(_history != null) _history.delete();
		if(_age != null) _age.delete();
		if(_temp != null) _temp.delete();
		if(_temp2 != null) _temp2.delete();
	}
}
