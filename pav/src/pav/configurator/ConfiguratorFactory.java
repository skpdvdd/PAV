package pav.configurator;

/**
 * Factory for retreiving Configurator objects. This is a static class.
 * 
 * @author christopher
 */
public class ConfiguratorFactory
{
	private static Generic _generic;
	private static Waveform _waveform;
	private static Spectrum _spectrum;
	private static Spectogram _spectogram;
	private static MelSpectrum _melSpectrum;
	private static Rainbow _rainbow;
	private static Phasor _phasor;
	private static Boxes _boxes;
	private static Bubbles _bubbles;
	
	/**
	 * Returns the generic configurator.
	 * 
	 * @return Generic configurator
	 */
	public static Generic generic()
	{
		if(_generic == null) {
			_generic = new Generic();
		}
		
		return _generic;
	}
	
	/**
	 * Returns the waveform configurator.
	 * 
	 * @return Waveform configurator
	 */
	public static Waveform waveform()
	{
		if(_waveform == null) {
			_waveform = new Waveform();
		}
		
		return _waveform;
	}
	
	/**
	 * Returns the spectrum configurator.
	 * 
	 * @return Spectrum configurator
	 */
	public static Spectrum spectrum()
	{
		if(_spectrum == null) {
			_spectrum = new Spectrum();
		}
		
		return _spectrum;
	}
	
	/**
	 * Returns the spectogram configurator.
	 * 
	 * @return Spectogram configurator
	 */
	public static Spectogram spectogram()
	{
		if(_spectogram == null) {
			_spectogram = new Spectogram();
		}
		
		return _spectogram;
	}
	
	/**
	 * Returns the melspectrum configurator.
	 * 
	 * @return Melspectrum configurator
	 */
	public static MelSpectrum melSpectrum()
	{
		if(_melSpectrum == null) {
			_melSpectrum = new MelSpectrum();
		}
		
		return _melSpectrum;
	}
	
	/**
	 * Returns the rainbow configurator.
	 * 
	 * @return Rainbow configurator
	 */
	public static Rainbow rainbow()
	{
		if(_rainbow == null) {
			_rainbow = new Rainbow();
		}
		
		return _rainbow;
	}
	
	/**
	 * Returns the phasor configurator.
	 * 
	 * @return Phasor configurator
	 */
	public static Phasor phasor()
	{
		if(_phasor == null) {
			_phasor = new Phasor();
		}
		
		return _phasor;
	}
	
	/**
	 * Returns the boxes configurator.
	 * 
	 * @return Boxes configurator
	 */
	public static Boxes boxes()
	{
		if(_boxes == null) {
			_boxes = new Boxes();
		}
		
		return _boxes;
	}
	
	/**
	 * Returns the bubbles configurator.
	 * 
	 * @return Bubbles configurator
	 */
	public static Bubbles bubbles()
	{
		if(_bubbles == null) {
			_bubbles = new Bubbles();
		}
		
		return _bubbles;
	}
	
	private ConfiguratorFactory() { }
}
