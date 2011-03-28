
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

package pav.audiosource;

import pav.Config;
import pav.lib.PAVException;

/**
 * An audio source.
 * 
 * @author christopher
 */
public abstract class AudioSource
{
	/**
	 * Creates an audio source based on the configuration.
	 * 
	 * @param callback The callback to use. Must not be null
	 * @return The audio source
	 * @throws PAVException On errors
	 */
	public static AudioSource factory(AudioCallback callback) throws PAVException
	{
		try {
			if(Config.audioSource.equals(Config.AUDIO_SOURCE_FIFO)) {
				return new FIFOAudioSource(callback);
			}
			else if(Config.audioSource.equals(Config.AUDIO_SOURCE_UDP)) {
				return new UDPAudioSource(callback);
			}
			else {
				throw new PAVException("Invalid audio source specified.");
			}
		}
		catch(Exception e) {
			throw new PAVException("Error while initializing audio source.", e);
		}
	}
	
	/**
	 * Starts reading (in a new thread).
	 */
	public abstract void read();
	
	/**
	 * Stops reading.
	 * 
	 * @throws InterruptedException If the thread was interrupted while waiting for the source to close
	 */
	public abstract void close() throws InterruptedException;
}
