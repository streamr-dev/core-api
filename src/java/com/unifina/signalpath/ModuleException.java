package com.unifina.signalpath;

import java.util.Collections;
import java.util.List;

public class ModuleException extends RuntimeException {
	private List<ModuleExceptionMessage> moduleExceptions;

	public ModuleException(Throwable cause, ModuleExceptionMessage moduleExceptionMessage) {
		this(cause.getMessage(), cause, Collections.singletonList(moduleExceptionMessage));
	}
	
    public ModuleException(String message, Throwable cause, List<ModuleExceptionMessage> moduleExceptions) {
        super(message, cause);
        this.moduleExceptions = moduleExceptions;
    }

	public List<ModuleExceptionMessage> getModuleExceptions() {
		return moduleExceptions;
	}
}
