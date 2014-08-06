package com.unifina.signalpath;

import java.util.List;

public class ModuleException extends RuntimeException {
	private List<ModuleExceptionMessage> moduleExceptions;
	
    public ModuleException(String message, Throwable cause, List<ModuleExceptionMessage> moduleExceptions) {
        super(message, cause);
        this.moduleExceptions = moduleExceptions;
    }

	public List<ModuleExceptionMessage> getModuleExceptions() {
		return moduleExceptions;
	}
}
