package pav.configurator;

import pav.Util;
import pav.lib.visualizer.Visualizer;

/**
 * Configurator for the Spectogram visualizer.
 * 
 * @author christopher
 */
public class Spectogram extends ConfiguratorAbstract
{
	@Override
	public boolean process(Visualizer subject, String query)
	{
		if(! (subject instanceof pav.lib.visualizer.Spectogram)) {
			return false;
		}
		
		String[] q = query.split(" ");
		
		if(q.length < 2) {
			return false;
		}
		
		if(q[0].equals("hightop")) {
			return _processHightop((pav.lib.visualizer.Spectogram) subject, Util.removeFirst(q));
		}
		
		if(q[0].equals("freq")) {
			return _processFreq((pav.lib.visualizer.Spectogram) subject, Util.removeFirst(q));
		}
		
		return false;
	}
	
	private boolean _processHightop(pav.lib.visualizer.Spectogram subject, String[] query)
	{
		subject.setHighOnTop(Util.parseBools(query)[0]);
		return true;
	}
	
	private boolean _processFreq(pav.lib.visualizer.Spectogram subject, String[] query)
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
