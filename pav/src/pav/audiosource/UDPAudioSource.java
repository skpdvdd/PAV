
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

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import pav.Config;

/**
 * UDP audio source.
 * 
 * @author christopher
 */
public class UDPAudioSource extends AudioSource implements Runnable
{
	private final AudioCallback _callback;
	private final DatagramSocket _socket;
	private final Thread _thread;
	private boolean _closed;
	
	/**
	 * Ctor.
	 * 
	 * @param callback The callback to use. Must not be null
	 * @throws SocketException If the socket could not be created
	 */
	public UDPAudioSource(AudioCallback callback) throws SocketException
	{
		_callback = callback;
		_socket = new DatagramSocket(Config.udpPort);
		_thread = new Thread(this, "UDPAudioSource");
	}
	
	@Override
	public void read()
	{
		_thread.start();
	}
	
	@Override
	public void run()
	{
		int ss2 = Config.sampleSize * 2;
		
		byte[] bb = new byte[ss2];
		DatagramPacket packet = new DatagramPacket(bb, bb.length);
		
		AudioStream stream = null;
		PipedOutputStream os = null;
		PipedInputStream is = null;
				
		try {
			os = new PipedOutputStream();
			is = new PipedInputStream(os);
			
			stream = new AudioStream(is, _callback);
			stream.read();
			
			while(! Thread.interrupted()) {
				_socket.receive(packet);				
				os.write(bb);
				packet.setLength(ss2);
			}
		}
		catch(IOException e) {
			if(! _closed) _callback.onError(e);
		}
		finally {
			_socket.close();
			
			try {
				if(stream != null) stream.close();
				if(os != null) os.close();
				if(is != null) is.close();
			}
			catch(Exception e) { }
		}
	}

	@Override
	public void close() throws InterruptedException
	{
		_closed = true;
		_thread.interrupt();
		_socket.close();
		_thread.join(250);
	}
}
