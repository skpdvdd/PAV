package pav.configurator;

import pav.Util;
import pav.lib.visualizer.Visualizer;

/**
 * Configurator for the MelSpectrum visualizer.
 * 
 * @author christopher
 */
public class MelSpectrum extends ConfiguratorAbstract
{
	@Override
	public boolean process(Visualizer subject, String query)
	{
		if(! (subject instanceof pav.lib.visualizer.MelSpectrum)) {
			return false;
		}
		
		String[] q = query.split(" ");
		
		if(q.length < 2) {
			return false;
		}
		
		if(q[0].equals("num")) {
			return _processNum((pav.lib.visualizer.MelSpectrum) subject, Util.removeFirst(q));
		}
		
		if(q[0].equals("bcolor")) {
			return _processBcolor((pav.lib.visualizer.MelSpectrum) subject, Util.removeFirst(q));
		}
		
		if(q[0].equals("quantize")) {
			return _processQuantize((pav.lib.visualizer.MelSpectrum) subject, Util.removeFirst(q));
		}
		
		return false;
	}
	
	private boolean _processNum(pav.lib.visualizer.MelSpectrum subject, String[] query)
	{
		int[] num = Util.tryParseInts(query);
		
		if(num.length != 1 || num[0] <= 0) return false;
		
		subject.setNumBands(num[0]);
		return true;
	}
	
	private boolean _processBcolor(pav.lib.visualizer.MelSpectrum subject, String[] query)
	{
		int[] c = Util.tryParseColors(query);
		
		if(c.length != 1) return false;
		
		subject.setBorderColor(c[0]);
		return true;
	}
	
	private boolean _processQuantize(pav.lib.visualizer.MelSpectrum subject, String[] query)
	{
		int[] q = Util.tryParseInts(query);
		
		if(q.length != 1) return false;
		
		subject.quantize(q[0]);
		return true;
	}
}
