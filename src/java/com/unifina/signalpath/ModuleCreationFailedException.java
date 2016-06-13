package com.unifina.signalpath;

/**
 * Exception that is thrown when instantiating or configuring a module failed.
 */
public class ModuleCreationFailedException extends RuntimeException {

	public ModuleCreationFailedException() {}

	public ModuleCreationFailedException(Throwable cause) {
		super(cause);
	}
}
