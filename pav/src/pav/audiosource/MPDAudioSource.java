package pav.audiosource;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import pav.Config;

/**
 * Audio source based on MPD FIFO output.
 * 
 * @author christopher
 */
public class MPDAudioSource implements AudioSource
{
	private final AudioCallback _callback;
	private final Thread _thread;
	
	/**
	 * Ctor.
	 * 
	 * @param callback The callback. Must not be null
	 */
	public MPDAudioSource(AudioCallback callback)
	{
		_callback = callback;
		_callback.onSampleRateChanged(44100);
				
		_thread = new Thread(this, "MPDAudioSource");
		_thread.start();
	}

	@Override
	public void run()
	{
		int sbl = Config.MPDAudioSource.sampleSize;
		short[] sb = new short[sbl];
		byte[] bb = new byte[sbl * 2];
		float[] frame = new float[sbl / 2];
		
		ByteBuffer bbuf = ByteBuffer.wrap(bb);
		bbuf.order(ByteOrder.BIG_ENDIAN);
		
		ShortBuffer sbuf = bbuf.asShortBuffer();		
		DataInputStream fifo = null;
		
		float normalize = (float) Short.MAX_VALUE;
		
		try {
			fifo = new DataInputStream(new FileInputStream(Config.MPDAudioSource.fifoPath));
			
			while(! Thread.interrupted()) {
				fifo.readFully(bb);
				sbuf.clear();
				sbuf.get(sb);
				
				int j = 0;
								
				for(int i = 0; i < sbl; i++) {					
					if(i % 2 == 1) {
						frame[j] = sb[i] / normalize;
						j++;
					}
				}
				
				_callback.onNewFrame(frame);
			}
		}
		catch(IOException e) {
			_callback.onError(e);
		}
		
		try { if(fifo != null) fifo.close(); } catch(IOException e) { }
	}
	
	@Override
	public void close() throws InterruptedException
	{
		_thread.interrupt();
		_thread.join(250);
	}
}
