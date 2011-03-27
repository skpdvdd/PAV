package pav.audiosource;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import pav.Config;

/**
 * Socket-based audio source.
 * 
 * @author christopher
 */
public class SocketAudioSource implements AudioSource
{
	private final Thread _thread;
	private final ServerSocket _serverSocket;
	private final AudioCallback _callback;

	private Socket _socket;
	private DataStream _dataStream;
	private Thread _dataStreamThread;
	private PrintWriter _out;
	private BufferedReader _in;
	private volatile boolean _done;
	
	/**
	 * Ctor.
	 * 
	 * @param callback The callback. Must not be null
	 * @throws IOException If the socket is blocked
	 */
	public SocketAudioSource(AudioCallback callback) throws IOException
	{
		_callback = callback;
		_callback.onSampleRateChanged(44100);
		_callback.onStatusChanged(new String[] { "No client connection." });
		
		_serverSocket = new ServerSocket(Config.SocketAudioSource.port, 0);
				
		_thread = new Thread(this, "SocketAudioSource");
		_thread.start();
	}

	@Override
	public void run()
	{
		while(! _done) {
			try {
				_socket = _serverSocket.accept();
				_out = new PrintWriter(_socket.getOutputStream(), true);
				_in = new BufferedReader(new InputStreamReader(_socket.getInputStream()));

				_dataStream = new DataStream();
				_dataStreamThread = new Thread(_dataStream, "SocketAudioSource DataStream");
				_dataStreamThread.start();
				_out.println("!ok " + _dataStream.getLocalPort());
				
				_callback.onStatusChanged(new String[0]);

				String line = null;

				while((line = _in.readLine()) != null) {
					String[] in = line.split(" ");

					if(in[0].equals("!close")) {
						break;
					}

					if(in[0].equals("!sr")) {
						try {
							_callback.onSampleRateChanged(Integer.parseInt(in[1]));
						}
						catch (Exception e) {
							_out.println("!err");
						}
					}
				}
			}
			catch (IOException e) {
				if(! _done) {
					_callback.onError(e);
				}
			}
			finally {
				_closeAll();
				_callback.onStatusChanged(new String[] { "No client connection." });
			}
		}

		try {
			_serverSocket.close();
		}
		catch (IOException e) { }
	}

	@Override
	public void close() throws InterruptedException
	{
		_done = true;
		_closeAll();
		_thread.join(250);
	}
	
	private void _closeAll()
	{
		if(_dataStream != null) {
			_dataStream.close();
		}

		if(_out != null) {
			_out.println("!close");
			_out.close();
		}

		if(_in != null) try { _in.close(); } catch (IOException e) { }
		if(_socket != null) try { _socket.close(); } catch (IOException e) { }
	}
	
	/**
	 * Represents a data stream from the client.
	 * 
	 * @author christopher
	 */
	private class DataStream implements Runnable
	{
		private Socket _socket;
		private volatile boolean _done;
		private final ServerSocket _serverSocket;

		/**
		 * Ctor.
		 * 
		 * Creates a new ServerSocket that listens to any free port.
		 * 
		 * @throws IOException If an error occured while initializing the ServerSocket
		 */
		public DataStream() throws IOException
		{
			_serverSocket = new ServerSocket(0, 0);
		}

		/**
		 * Returns the port the ServerSocket listens to.
		 * 
		 * @return The port
		 */
		public int getLocalPort()
		{
			return _serverSocket.getLocalPort();
		}

		@Override
		public void run()
		{
			DataInputStream in = null;
			FloatBuffer frameBuffer = null;
			byte[] byteIn = new byte[0];
			float[] frame = new float[0];
			
			try {
				_socket = _serverSocket.accept();
				in = new DataInputStream(_socket.getInputStream());

				while(! _done) {
					int frameLen = in.readInt();
					int frameLenBytes = frameLen * 4;
					
					if(byteIn.length != frameLenBytes) {
						byteIn = new byte[frameLenBytes];
						frameBuffer = ByteBuffer.wrap(byteIn).asFloatBuffer();
						frame = new float[frameLen];
					}
					
					in.readFully(byteIn);
					
					frameBuffer.clear();
					frameBuffer.get(frame, 0, frameLen);
					
					_callback.onNewFrame(frame);
				}
			}
			catch (Exception e) {
				if(! _done) {
					_callback.onError(e);
				}
			}
			finally {
				if(in != null) {
					try {
						in.close();
					}
					catch(IOException e) { }
				}
				
				_closeAll();
			}
		}

		/**
		 * Closes the connection.
		 */
		public void close()
		{
			_done = true;
			_closeAll();
		}
		
		private void _closeAll()
		{
			try { _serverSocket.close(); } catch (IOException e) { }
			if(_socket != null) try { _socket.close(); } catch (IOException e) { }
		}
	}
}
