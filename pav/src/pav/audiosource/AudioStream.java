
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

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import pav.Config;

/**
 * An audio stream.
 * 
 * @author christopher
 */
public class AudioStream implements Runnable
{
	private final Thread _thread;
	private final DataInputStream _is;
	private final AudioCallback _callback;
	
	/**
	 * Ctor.
	 * 
	 * @param source The input to read from. Must not be null and readable
	 * @param callback The callback to send new frames to. Must not be null
	 */
	public AudioStream(InputStream source, AudioCallback callback)
	{
		_callback = callback;
		_is = new DataInputStream(source);
		
		_thread = new Thread(this, "AudioStream");
	}
	
	/**
	 * Starts reading (in a new thread).
	 */
	public void read()
	{
		_thread.start();
	}
	
	public void waitUntilFinished() throws InterruptedException
	{
		_thread.join();
	}
	
	@Override
	public void run()
	{		
		try {
			if(Config.sampleFormat.equals(Config.SAMPLE_FORMAT_INT8)) {
				_processingShort();
			}
			else if(Config.sampleFormat.equals(Config.SAMPLE_FORMAT_FLOAT)) {
				_processFloat();
			}
		}
		catch(IOException e) {
			_callback.onError(e);
		}
		finally {
			try { _is.close(); } catch(IOException e) { }
		}
	}
	
	/**
	 * Closes the audio stream.
	 * 
	 * @throws InterruptedException If the thread was interrupted while waiting for the stream to close
	 */
	public void close() throws InterruptedException
	{
		_thread.interrupt();
		_thread.join(250);
	}
	
	private void _processFloat() throws IOException
	{
		int ss = Config.sampleSize;
		byte[] bb = new byte[ss * 4];
		float[] frame = new float[ss];

		ByteBuffer bbuf = ByteBuffer.wrap(bb);
		bbuf.order(Config.byteOrder);		
		FloatBuffer fbuf = bbuf.asFloatBuffer();		

		while(! Thread.interrupted()) {
			_is.readFully(bb);
			
			fbuf.clear();
			fbuf.get(frame);
							
			_callback.onNewFrame(frame);
		}
	}
	
	private void _processingShort() throws IOException
	{
		int ss = Config.sampleSize;
		short[] sb = new short[ss];
		byte[] bb = new byte[ss * 2];
		float[] frame = new float[ss];
		float normalize = (float) Short.MAX_VALUE;

		ByteBuffer bbuf = ByteBuffer.wrap(bb);
		bbuf.order(Config.byteOrder);		
		ShortBuffer sbuf = bbuf.asShortBuffer();		

		while(! Thread.interrupted()) {
			_is.readFully(bb);
			
			sbuf.clear();
			sbuf.get(sb);
			
			for(int i = 0; i < ss; i++) {
				frame[i] = sb[i] / normalize;
			}
				
			_callback.onNewFrame(frame);
		}
	}
}
