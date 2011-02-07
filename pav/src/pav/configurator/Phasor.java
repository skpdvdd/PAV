package pav.configurator;

import pav.Util;
import pav.lib.visualizer.Visualizer;

/**
 * Configurator for the Phasor visualizer.
 * 
 * @author christopher
 */
public class Phasor extends ConfiguratorAbstract
{
	@Override
	public boolean process(Visualizer subject, String query)
	{
		if(! (subject instanceof pav.lib.visualizer.Phasor)) {
			return false;
		}
		
		String[] q = query.split(" ");
		
		if(q.length < 2) {
			return false;
		}
		
		if(q[0].equals("mode")) {
			return _processMode((pav.lib.visualizer.Phasor) subject, Util.removeFirst(q));
		}
		
		return false;
	}
	
	private boolean _processMode(pav.lib.visualizer.Phasor subject, String[] query)
	{
		if(query[0].equals("dots")) {
			subject.setMode(pav.lib.visualizer.Phasor.MODE_DOTS);
		}
		else if(query[0].equals("lines")) {
			subject.setMode(pav.lib.visualizer.Phasor.MODE_LINES);
		}
		else if(query[0].equals("curves")) {
			subject.setMode(pav.lib.visualizer.Phasor.MODE_CURVES);
		}
		else {
			return false;
		}
		
		return true;
	}
}
