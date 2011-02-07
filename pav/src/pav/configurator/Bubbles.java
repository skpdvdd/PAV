package pav.configurator;

import pav.Util;
import pav.lib.visualizer.Visualizer;

/**
 * Configurator for the Bubbles visualizer.
 * 
 * @author christopher
 */
public class Bubbles extends ConfiguratorAbstract
{
	@Override
	public boolean process(Visualizer subject, String query)
	{
		if(! (subject instanceof pav.lib.visualizer.Bubbles)) {
			return false;
		}
		
		String[] q = query.split(" ");
		
		if(q.length < 2) {
			return false;
		}
		
		if(q[0].equals("darken")) {
			return _processDarken((pav.lib.visualizer.Bubbles) subject, Util.removeFirst(q));
		}
		
		if(q[0].equals("size")) {
			return _processSize((pav.lib.visualizer.Bubbles) subject, Util.removeFirst(q));
		}
		
		if(q[0].equals("bloom")) {
			return _processBloom((pav.lib.visualizer.Bubbles) subject, Util.removeFirst(q));
		}
		
		if(q[0].equals("rate")) {
			return _processRate((pav.lib.visualizer.Bubbles) subject, Util.removeFirst(q));
		}
		
		return false;
	}
	
	private boolean _processDarken(pav.lib.visualizer.Bubbles subject, String[] query)
	{
		float[] darken = Util.tryParseFloats(query);
		
		if(darken.length != 1 || darken[0] <= 0) return false;
		
		subject.setDarkenFactor(darken[0]);
		return true;
	}
	
	private boolean _processSize(pav.lib.visualizer.Bubbles subject, String[] query)
	{
		float[] size = Util.tryParseFloats(query);
		
		if(size.length != 2 || size[0] <= 0 || size[0] > size[1] || size[1] > 0.5f) return false;
		
		subject.setBubbleSize(size[0], size[1]);
		return true;
	}
	
	private boolean _processBloom(pav.lib.visualizer.Bubbles subject, String[] query)
	{
		boolean[] bloom = Util.parseBools(query);
		
		if(bloom.length != 1) return false;
		
		subject.useBloom(bloom[0]);
		return true;
	}
	
	private boolean _processRate(pav.lib.visualizer.Bubbles subject, String[] query)
	{
		float[] rate = Util.parseFloats(query);
		
		if(rate.length != 2 || rate[0] <= 0 || rate[0] > rate[1] || rate[1] > 5) return false;
		
		subject.setSpawnRate(rate[0], rate[1]);
		return true;
	}
}
