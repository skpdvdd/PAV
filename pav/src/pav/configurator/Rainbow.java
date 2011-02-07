package pav.configurator;

import pav.Util;
import pav.lib.visualizer.Visualizer;

/**
 * Configurator for the Rainbow visualizer.
 * 
 * @author christopher
 */
public class Rainbow extends ConfiguratorAbstract
{
	@Override
	public boolean process(Visualizer subject, String query)
	{
		if(! (subject instanceof pav.lib.visualizer.Rainbow)) {
			return false;
		}
		
		String[] q = query.split(" ");
		
		if(q.length < 2) {
			return false;
		}
		
		if(q[0].equals("mode")) {
			return _processMode((pav.lib.visualizer.Rainbow) subject, Util.removeFirst(q));
		}
		
		return false;
	}
	
	private boolean _processMode(pav.lib.visualizer.Rainbow subject, String[] query)
	{
		if(query[0].equals("frequency")) {
			subject.setMode(pav.lib.visualizer.Rainbow.MODE_FREQUENCY);
		}
		else if(query[0].equals("intensity")) {
			subject.setMode(pav.lib.visualizer.Rainbow.MODE_INTENSITY);
		}
		else {
			return false;
		}
		
		return true;
	}
}
