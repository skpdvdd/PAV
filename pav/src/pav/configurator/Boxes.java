package pav.configurator;

import pav.Util;
import pav.lib.visualizer.Visualizer;
import processing.core.PApplet;

/**
 * Configurator for the Boxes visualizer.
 * 
 * @author christopher
 */
public class Boxes extends ConfiguratorAbstract
{
	@Override
	public boolean process(Visualizer subject, String query)
	{
		if(! (subject instanceof pav.lib.visualizer.Boxes)) {
			return false;
		}
		
		String[] q = query.split(" ");
		
		if(q.length < 2) {
			return false;
		}
		
		if(q[0].equals("rotate")) {
			return _processRotate((pav.lib.visualizer.Boxes) subject, Util.removeFirst(q));
		}
		
		if(q[0].equals("bcolor")) {
			return _processBcolor((pav.lib.visualizer.Boxes) subject, Util.removeFirst(q));
		}
		
		if(q[0].equals("quantize")) {
			return _processQuantize((pav.lib.visualizer.Boxes) subject, Util.removeFirst(q));
		}
		
		if(q[0].equals("maxheight")) {
			return _processMaxHeight((pav.lib.visualizer.Boxes) subject, Util.removeFirst(q));
		}
		
		return false;
	}
	
	private boolean _processRotate(pav.lib.visualizer.Boxes subject, String[] query)
	{
		float[] factor = Util.tryParseFloats(query);
		
		if(factor.length != 1 || Math.abs(factor[0]) > 5) return false;
		
		subject.rotate(factor[0] * PApplet.PI / 360);
		return true;
	}
	
	private boolean _processBcolor(pav.lib.visualizer.Boxes subject, String[] query)
	{
		int[] c = Util.tryParseColors(query);
		
		if(c.length != 1) return false;
		
		subject.setEdgeColor(c[0]);
		return true;
	}
	
	private boolean _processQuantize(pav.lib.visualizer.Boxes subject, String[] query)
	{
		int[] q = Util.tryParseInts(query);
		
		if(q.length != 1) return false;
		
		subject.quantize(q[0]);
		return true;
	}
	
	private boolean _processMaxHeight(pav.lib.visualizer.Boxes subject, String[] query)
	{
		int[] q = Util.tryParseInts(query);
		
		if(q.length != 1 || q[0] <= 0) return false;

		subject.setMaxHeight(q[0]);
		return true;
	}
}
