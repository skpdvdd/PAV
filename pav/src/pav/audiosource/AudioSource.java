package pav.audiosource;

/**
 * An audio source.
 * 
 * @author christopher
 */
public interface AudioSource extends Runnable
{
	/**
	 * Closes the audio source.
	 * 
	 * @throws InterruptedException If the thread was interrupted while waiting for the source to close
	 */
	void close() throws InterruptedException;
}
