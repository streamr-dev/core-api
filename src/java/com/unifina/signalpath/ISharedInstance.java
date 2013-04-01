package com.unifina.signalpath;

/**
 * AbstractSignalPathModules that implement the ISharedInstance interface should not
 * be always instantiated anew, but instead it should be checked
 * whether an instance with the same sharedInstanceID exists in a shared collection.
 */
public interface ISharedInstance {
	/**
	 * 
	 */
	public Object getSharedInstanceID();
}
