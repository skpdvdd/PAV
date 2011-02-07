package pav.configurator;

import pav.Util;
import pav.lib.visualizer.Visualizer;
import processing.core.PApplet;

/**
 * Configurator for all visualizers.
 * 
 * @author christopher
 */
public class Generic extends ConfiguratorAbstract
{
	@Override
	public boolean process(Visualizer subject, String query)
	{
		String[] q = query.split(" ");
		
		if(q.length < 2) {
			return false;
		}
		
		if(q[0].equals("color")) {
			return _processColor(subject, Util.removeFirst(q));
		}
		
		if(q[0].equals("area")) {
			return _processArea(subject, Util.removeFirst(q));
		}
		
		return false;
	}
	
	private boolean _processColor(Visualizer subject, String[] query)
	{
		int[] colors = Util.tryParseColors(query);
		
		if(colors.length == 0) return false;
		
		if(colors.length == 1) {
			subject.setColor(colors[0]);
		}
		else if(colors.length == 2) {
			subject.setColor(colors[0], colors[1], PApplet.RGB);
		}
		else {
			float[] thresholds = new float[colors.length];
			float delta = (float) (1.0 / (thresholds.length - 1));
			
			for(int i = 0; i < thresholds.length; i++) {
				thresholds[i] = i * delta;
			}
			
			subject.setColor(thresholds, colors, PApplet.RGB);
		}
		
		return true;
	}
	
	private boolean _processArea(Visualizer subject, String[] query)
	{
		float[] coords = Util.tryParseFloats(query);
		
		if(coords.length != 4) return false;
		
		for(float v : coords) {
			if(v < 0 || v > 1) return false;
		}
		
		if(coords[2] <= coords[0]) return false;
		if(coords[3] <= coords[1]) return false;
		
		subject.setArea(coords[0], coords[1], coords[2], coords[3], true);
		
		return true;
	}
}
