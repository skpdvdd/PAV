package pav.configurator;

import pav.Util;
import pav.lib.visualizer.Visualizer;

/**
 * Configurator for the Waveform visualizer.
 * 
 * @author christopher
 */
public class Waveform extends ConfiguratorAbstract
{
	@Override
	public boolean process(Visualizer subject, String query)
	{
		if(! (subject instanceof pav.lib.visualizer.Waveform)) {
			return false;
		}
		
		String[] q = query.split(" ");
		
		if(q.length < 2) {
			return false;
		}
		
		if(q[0].equals("mode")) {
			return _processMode((pav.lib.visualizer.Waveform) subject, Util.removeFirst(q));
		}
		
		return false;
	}
	
	private boolean _processMode(pav.lib.visualizer.Waveform subject, String[] query)
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
}
