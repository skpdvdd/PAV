package pav.configurator;

import pav.lib.visualizer.Visualizer;

/**
 * Allows runtime configuration of a visualizer via a query entered by the user.
 */
public interface Configurator
{
	/**
	 * Processes a configuration request.
	 * 
	 * @param subject The visualizer to configure. Must not be null
	 * @param query The user query. Must not be null
	 * @return Whether this configurator was able to handle the configuration request
	 */
	boolean process(Visualizer subject, String query);
}
