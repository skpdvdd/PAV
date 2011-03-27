package pav.audiosource;

/**
 * Audio Callback used by audio sources.
 * 
 * @author christopher
 */
public interface AudioCallback
{
	/**
	 * Called on new audio frames.
	 * 
	 * @param frame The frame. Must not be null
	 */
	void onNewFrame(float[] frame);
	
	/**
	 * Called on song changes.
	 */
	void onSongChanged();
	
	/**
	 * Called on sample rate changes.
	 * 
	 * @param sampleRate The new sample rate.
	 */
	void onSampleRateChanged(int sampleRate);
	
	/**
	 * Called on status changes.
	 * 
	 * @param status Status info
	 */
	void onStatusChanged(String[] status);
	
	/**
	 * Called on errors.
	 * 
	 * @param error The error
	 */
	void onError(Throwable error);
}
