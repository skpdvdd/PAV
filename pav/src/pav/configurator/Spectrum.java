package pav.configurator;

import pav.Util;
import pav.lib.visualizer.Visualizer;

/**
 * Configurator for the Spectrum visualizer.
 * 
 * @author christopher
 */
public class Spectrum extends ConfiguratorAbstract
{	
	@Override
	public boolean process(Visualizer subject, String query)
	{
		if(! (subject instanceof pav.lib.visualizer.Spectrum)) {
			return false;
		}
		
		String[] q = query.split(" ");
		
		if(q.length < 2) {
			return false;
		}
		
		if(q[0].equals("mode")) {
			return _processMode((pav.lib.visualizer.Spectrum) subject, Util.removeFirst(q));
		}
		
		if(q[0].equals("freq")) {
			return _processFreq((pav.lib.visualizer.Spectrum) subject, Util.removeFirst(q));
		}
		
		return false;
	}
	
	private boolean _processMode(pav.lib.visualizer.Spectrum subject, String[] query)
	{
		if(query[0].equals("bins")) {
			subject.setMode(pav.lib.visualizer.Waveform.MODE_BINS);
			return true;
		}
		else if(query[0].equals("dots")) {
			subject.setMode(pav.lib.visualizer.Waveform.MODE_DOTS);
			return true;
		}
		else if(query[0].equals("shape")) {
			subject.setMode(pav.lib.visualizer.Waveform.MODE_SHAPE);
			return true;
		}
		
		return false;
	}
	
	private boolean _processFreq(pav.lib.visualizer.Spectrum subject, String[] query)
	{
		int[] freqs = Util.tryParseInts(query);
		
		if(freqs.length == 1) {
			if(freqs[0] == 0) {
				subject.noCutoffFrequencies();
				return true;
			}
			else if(freqs[0] > 0) {
				subject.setCutoffFrequencies(0, freqs[0]);
				return true;
			}
		}
		else if(freqs.length == 2) {
			if(freqs[1] > freqs[0] && freqs[0] >= 0) {
				subject.setCutoffFrequencies(freqs[0], freqs[1]);
				return true;
			}
		}
		
		return false;
	}
}
