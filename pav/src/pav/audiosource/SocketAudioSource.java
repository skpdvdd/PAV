
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
import java.net.ServerSocket;
import java.net.Socket;
import pav.Config;

/**
 * Socket-based audio source.
 * 
 * @author christopher
 */
public class SocketAudioSource extends AudioSource implements Runnable
{
	private final Thread _thread;
	private final ServerSocket _serverSocket;
	private final AudioCallback _callback;
	private AudioStream _stream;
	
	/**
	 * Ctor.
	 * 
	 * @param callback The callback. Must not be null
	 * @throws IOException If the socket is blocked
	 */
	public SocketAudioSource(AudioCallback callback) throws IOException
	{
		_callback = callback;
		_serverSocket = new ServerSocket(Config.socketPort, 0);		
		_thread = new Thread(this, "SocketAudioSource");
	}
	
	@Override
	public void read()
	{
		_thread.start();
	}

	@Override
	public void run()
	{
		while(! Thread.interrupted()) {
			_callback.onStatusChanged(new String[] { "No client connection." });
			
			try {
				Socket socket = _serverSocket.accept();
				
				_stream = new AudioStream(socket.getInputStream(), _callback);
				_stream.read();
				_callback.onStatusChanged(new String[0]);
				_stream.waitUntilFinished();
			}
			catch (IOException e) {
				_callback.onError(e);
			}
			catch(InterruptedException e) { }
			finally {
				try { _stream.close(); } catch(InterruptedException e) { e.printStackTrace(); }
			}
		}

		try { _serverSocket.close(); } catch (IOException e) { }
	}

	@Override
	public void close() throws InterruptedException
	{
		_thread.interrupt();
		
		try { _closeAll(); } catch(IOException e) { e.printStackTrace(); }
		
		_thread.join(125);
	}
	
	private void _closeAll() throws InterruptedException, IOException
	{
		_serverSocket.close();
		if(_stream != null) _stream.close();
	}
}
