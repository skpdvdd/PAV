
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

/**
 * A MEL filter bank.
 * 
 * @author christopher
 */
public class MelFilterBank
{
	private final int _minFrequency, _maxFrequency, _numFilters;
	private final float _minMel, _maxMel, _melDelta;
	private final MelFilter[] _filters;
	
	/**
	 * Ctor.
	 * 
	 * @param minFrequency The minimum frequency (in Hz) the bank should process. Must be >= 0
	 * @param maxFrequency The maximum frequency (in Hz) the bank should process. Must be > minFrequency
	 * @param numFilters The number of filters to use. Must be > 0
	 */
	public MelFilterBank(int minFrequency, int maxFrequency, int numFilters)
	{
		_minFrequency = minFrequency;
		_maxFrequency = maxFrequency;
		_numFilters = numFilters;
		
		_filters = new MelFilter[numFilters];
		
		_minMel = freqToMel(_minFrequency);
		_maxMel = freqToMel(_maxFrequency);
		_melDelta = (_maxMel - _minMel) / (_numFilters + 1);
		
		float minMel = _minMel;
		float centerMel = _minMel + _melDelta;
		float maxMel = centerMel + _melDelta;
		
		for(int i = 0; i < numFilters; i++) {
			_filters[i] = new MelFilter(minMel, centerMel, maxMel);
			
			minMel += _melDelta;
			centerMel += _melDelta;
			maxMel += _melDelta;
		}
	}
	
	/**
	 * Filters a given spectrum.
	 * 
	 * @param spectrum The input spectrum. Must not be null
	 * @param sampleRate The sample rate of the audio data. Must be > 0
	 * @return The filtered values
	 */
	public float[] filter(float[] spectrum, float sampleRate)
	{
		int l = spectrum.length;
		int fl = _filters.length;
		float fDelta = (sampleRate / 2) / l;
		float fDelta2 = fDelta / 2;
		
		float[] mel = new float[l];
		float[] out = new float[fl];
		
		for(int i = 0; i < l; i++) {
			mel[i] = freqToMel(i * fDelta + fDelta2);
		}
		
		for(int i = 0; i < fl; i++) {
			out[i] = _filters[i].filter(mel, spectrum);
		}
		
		return out;
	}
	
	/**
	 * Calculates the Mel out of a frequency in Hz.
	 * 
	 * @param freq The input frequency. Must be >= 0
	 * @return The Mel
	 */
	public static float freqToMel(float freq)
	{
		return (float) (1127 * Math.log(1 + freq / 700));
	}
	
	/**
	 * Calculates the frequency in Hz out of a given Mel.
	 * 
	 * @param mel The mel. Must be > 0
	 * @return The frequency
	 */
	public static float melToFreq(float mel)
	{
		return (float) (700 * (Math.exp(mel / 1127) - 1));
	}
	
	/**
	 * A Mel filter.
	 * 
	 * @author christopher
	 */
	private class MelFilter
	{
		private final float _melMin, _melCenter, _melMax, _vMax;
				
		/**
		 * Ctor.
		 * 
		 * @param melMin The min mel this filter should process. Must be >= 0
		 * @param melCenter The center mel this filter should process. Must be > melMin
		 * @param melMax The max mel this filter should process. Must be > melCenter
		 */
		public MelFilter(float melMin, float melCenter, float melMax)
		{
			_melMin = melMin;
			_melCenter = melCenter;
			_melMax = melMax;
			_vMax = 2 / (melToFreq(melMax) - melToFreq(melMin));
		}
		
		/**
		 * Processes a number of given mel frequencies.
		 * 
		 * @param mels A number of mel frequencies. Must not be null
		 * @param intensities The intensities of the mel frequencies. Must not be null and of same length as mels
		 * @return The accumulated intensity of this filter
		 */
		public float filter(float[] mels, float[] intensities)
		{
			float intensity = 0;
			
			int l = mels.length;
			
			for(int i = 0; i < l; i++) {
				float v = mels[i];
				
				if(v < _melMin) continue;
				if(v > _melMax) break;
				
				float k = (v <= _melCenter) ? (_melCenter - v) / (_melCenter - _melMin) : 1 - ((v - _melCenter) / (_melMax - _melCenter));
				
				intensity += intensities[i] * k * _vMax;
			}
			
			return intensity;
		}
	}
}
