
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

package pav.lib.frame;

import java.util.HashMap;
import processing.core.PApplet;
import ddf.minim.analysis.FFT;

/**
 * Provides access to the current signal frame aswell as
 * descriptors and transform functionality. This is a static class.
 * 
 * @author christopher
 */
public class Frame
{
	private static float _sampleRate;
	private static float[] _samples;
	private static float[] _samplesWindowed;
		
	/**
	 * Updates the current frame.
	 * 
	 * @param samples The samples that make up the new frame. Must not be null
	 */
	public static void update(float[] samples)
	{
		_samples = samples;
		_samplesWindowed = null;
		
		Descriptor._reset();
		Transform._reset();
	}
	
	/**
	 * Sets the rate the frame was sampled with.
	 * 
	 * @param rate The sample rate. Must be > 0
	 */
	public static void setSampleRate(float rate)
	{
		_sampleRate = rate;
	}
	
	/**
	 * Gets the sample rate the frame was sampled with.
	 * 
	 * @return The sample rate
	 */
	public static float getSampleRate()
	{
		return _sampleRate;
	}
	
	/**
	 * Gets the samples of the frame. Must not be modified.
	 * 
	 * @return The samples
	 */
	public static float[] samples()
	{
		return _samples;
	}
	
	/**
	 * Gets the samples of the frame, windowed with a Hamming window.
	 * 
	 * @return The windowed samples
	 */
	public static float[] samplesWindowed()
	{
		if(_samplesWindowed != null) {
			return _samplesWindowed;
		}
		
		int len = _samples.length;
		int lenDec = len - 1;
		
		_samplesWindowed = new float[len];
		
		for(int i = 0; i < len; i++) {
			_samplesWindowed[i] = _samples[i] * (0.54f - 0.46f * (float) Math.cos(PApplet.TWO_PI * i / lenDec));
		}
		
		return _samplesWindowed;
	}
	
	/**
	 * Frame descriptors.
	 * 
	 * All descriptors are calculated based on the windows version of the current frame.
	 * 
	 * @author christopher
	 */
	public static class Descriptor
	{
		private static Float _amplitudeMax;
		private static Float _rms;
		private static Integer _zeroCrossings;
		private static Float _zeroCrossingRate;
		private static Float _spectralCentroid;		
		
		/**
		 * Calculates the amplitude maximum, that is the maximum of the
		 * absolutes of the values of the frame. Values range from 0 to 1.
		 * 
		 * @return The amplitude maximum
		 */
		public static float amplitudeMax()
		{
			if(_amplitudeMax != null) {
				return _amplitudeMax;
			}
			
			float[] samples = samplesWindowed();
			int l = samples.length;
			float max = Float.MIN_VALUE;

			for(int i = 0; i < l; i++) {
				float v = Math.abs(samples[i]);
				
				if(v > max) {
					max = v;
					
					if(max == 1) {
						break;
					}
				}
			}
			
			_amplitudeMax = max;	
			
			return _amplitudeMax;
		}

		/**
		 * Calculates the root mean square (RMS).
		 * 
		 * @return The RMS value
		 */
		public static float rms()
		{
			if(_rms != null) {
				return _rms;
			}
			
			float[] samples = samplesWindowed();
			float v = 0;
			float l = samples.length;
			
			for(int i = 0; i < l; i++) {
				v += samples[i] * samples[i];
			}
			
			v = v / l;
			_rms = (float) Math.sqrt(v);
			
			return _rms;
		}

		/**
		 * Calculates the number of sign changes.
		 * 
		 * @return The number of sign changes
		 */
		public static int zeroCrossings()
		{
			if(_zeroCrossings != null) {
				return _zeroCrossings;
			}
			
			int n = 0;
			int l = _samples.length;
			boolean p = (_samples[0] > 0);
			
			for(int i = 1; i < l; i++) {
				float v = _samples[i];
				boolean c = v > 0;
				
				if(v == 0) {
					continue;
				}
				
				if(c != p) {
					n++;
				}
				
				p = c;
			}
			
			_zeroCrossings = n;
			
			return _zeroCrossings;
		}

		/**
		 * Calculates the zero crossing rate (ZCR).
		 * 
		 * @return The ZCR
		 */
		public static float zeroCrossingRate()
		{
			if(_zeroCrossingRate != null) {
				return _zeroCrossingRate;
			}
			
			_zeroCrossingRate = (zeroCrossings() * _sampleRate) / (2 * _samples.length);
			
			return _zeroCrossingRate;
		}
		
		/**
		 * Calculates the spectral centroid.
		 * 
		 * @return The spectral centroid
		 */
		public static float spectralCentroid()
		{
			if(_spectralCentroid != null) {
				return _spectralCentroid;
			}
			
			FFT fft = Transform._fft();
			int l = fft.specSize();
			float sum = 0, centroid = 0;
			
			for(int i = 0; i < l; i++) {
				centroid += fft.indexToFreq(i) * fft.getBand(i);
				sum += fft.getBand(i);
			}
			
			_spectralCentroid = (sum > 0) ? centroid / sum : 0;
			
			return _spectralCentroid;
		}
		
		/**
		 * Resets all descriptors calculated. This must be called immediately after the
		 * frame was updated.
		 */
		private static void _reset()
		{
			_amplitudeMax = null;
			_rms = null;
			_zeroCrossings = null;
			_zeroCrossingRate = null;	
			_spectralCentroid = null;
		}
	}
		
	/**
	 * Frame transforms.
	 * 
	 * All transforms are based on the spectrum of the windowed version of the current frame.
	 * 
	 * @author christopher
	 */
	public static class Transform
	{
		private static FFT _fft;
		private static float _fftSampleRate;
		private static boolean _fftForwarded;
		private static HashMap<String, MelFilterBank> _melFilterBanks;
		
		private static TransformResult _spectrum;
		private static HashMap<Integer, TransformResult> _melSpectrums;
				
		/**
		 * Returns the logarithm of the frequency intensity distribution of the frame.
		 * The result must not be modified.
		 * 
		 * @return The transformed frame
		 */
		public static TransformResult spectrum()
		{
			if(_spectrum != null) {
				return _spectrum;
			}
			
			FFT fft = _fft();
			int l = fft.specSize();
			float min = Float.MAX_VALUE;
			float max = Float.MIN_VALUE;
			
			float[] transform = new float[l];
			
			for(int i = 0; i < l; i++) {
				float v = (float) Math.log10(fft.getBand(i) + 1);

				if(v < min) min = v;
				if(v > max) max = v;
				
				transform[i] = v;
			}
			
			_spectrum = new TransformResult(transform, min, max);
			
			return _spectrum;
		}
		
		/**
		 * Calculates the mel spectrum of the current frame.
		 * The result must not be modified.
		 * 
		 * @param numBands The number of mel scale bands to use. Must be > 0
		 * @return The transformed frame
		 */
		public static TransformResult melSpectrum(int numBands)
		{
			if(_melSpectrums == null) {
				_melSpectrums = new HashMap<Integer, TransformResult>();
			}
			
			if(_melFilterBanks == null) {
				_melFilterBanks = new HashMap<String, MelFilterBank>();
			}
			
			if(_melSpectrums.containsKey(numBands)) {
				return _melSpectrums.get(numBands);
			}
			
			int fMax = Math.round(_sampleRate / 2);
			String hash = numBands + "-0-" + fMax;
			
			if(! _melFilterBanks.containsKey(hash)) {
				_melFilterBanks.put(hash, new MelFilterBank(0, fMax, numBands));
			}
			
			float[] mels = _melFilterBanks.get(hash).filter(spectrum().frame(), _sampleRate);
			
			float min = Float.MAX_VALUE;
			float max = Float.MIN_VALUE;
			
			for(int i = 0; i < numBands; i++) {
				float v = mels[i];
				
				if(v < min) min = v;
				if(v > max) max = v;
			}
			
			TransformResult result = new TransformResult(mels, min, max);
			_melSpectrums.put(numBands, result);
			
			return result;
		}

		/**
		 * Returns an FFT based on the settings of the song and frame currently played.
		 * The FFT already has the updated data, that is forward() was already called.
		 * By default, no averages are calculated by this fft. If averages are needed,
		 * set noAverages() again after calculation.
		 * 
		 * @return
		 */
		private static FFT _fft()
		{
			if(_fft != null) {
				if(_fft.timeSize() != _samples.length || _fftSampleRate != _sampleRate) {
					_fft = null;
				}
				else {
					if(! _fftForwarded) {
						_fft.forward(samplesWindowed());
						_fftForwarded = true;
					}
					
					return _fft;
				}
			}
			
			_fft = new FFT(_samples.length, _sampleRate);
			_fft.noAverages();
			_fft.forward(samplesWindowed());
			_fftSampleRate = _sampleRate;
			_fftForwarded = true;
			
			return _fft;
		}
		
		/**
		 * Resets all calculated transforms. This must be called immediately after
		 * the frame was updated.
		 */
		private static void _reset()
		{
			_spectrum = null;
			_fftForwarded = false;
			
			if(_melSpectrums != null) _melSpectrums.clear();
		}
		
		/**
		 * Frame transform related utility methods.
		 * 
		 * @author christopher
		 */
		public static class Util
		{
			/**
			 * Returns the center frequency of the frequency band with the specified index.
			 * 
			 * @param band The index of the frequency band
			 * @return The center frequency
			 */
			public static float bandToFrequency(int band)
			{
				return _fft().indexToFreq(band);
			}
			
			/**
			 * Returns the frequency band the given frequency would belong to.
			 * 
			 * @param frequency The frequency
			 * @return The band the frequency belongs to
			 */
			public static int frequencyToBand(float frequency)
			{
				return _fft().freqToIndex(frequency);
			}
		}
	}
		
	private Frame() { }
}
