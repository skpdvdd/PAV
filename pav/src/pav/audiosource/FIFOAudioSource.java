
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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import pav.Config;

/**
 * FIFO-based audio source.
 * 
 * @author christopher
 */
public class FIFOAudioSource extends AudioSource
{
	private final AudioStream _stream;
	
	/**
	 * Ctor.
	 * 
	 * @param callback The callback. Must not be null
	 * @throws FileNotFoundException If the fifo does not exist
	 */
	public FIFOAudioSource(AudioCallback callback) throws FileNotFoundException
	{
		_stream = new AudioStream(new FileInputStream(Config.fifoPath), callback);
	}
	
	@Override
	public void read()
	{
		_stream.read();
	}
	
	@Override
	public void close() throws InterruptedException
	{
		_stream.close();
	}
}
