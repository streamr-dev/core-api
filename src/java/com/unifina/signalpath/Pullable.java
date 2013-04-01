package com.unifina.signalpath;

/**
 * An interface for AbstractSignalPathModules, from which a value can be pulled at any given time.
 * Motivation behind this was the "lazy-init" pull mechanism used for Parameters connected to Constants.
 * @author Henri
 *
 */
public interface Pullable<T> {
	/**
	 * Pulls the best-effort value for the specified output from an AbstractSignalPathModule. 
	 * @param output
	 * @return
	 */
	T pullValue(Output output);
}
