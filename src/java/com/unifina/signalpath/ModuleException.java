package com.unifina.signalpath;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModuleException extends RuntimeException {
	private List<ModuleExceptionMessage> moduleExceptions;
	
    public ModuleException(String message, Throwable cause, List<ModuleExceptionMessage> moduleExceptions) {
        super(message, cause);
        this.moduleExceptions = moduleExceptions;
    }

	public List<ModuleExceptionMessage> getModuleExceptions() {
		return moduleExceptions;
	}

	public Map<String, Object> toMap() {
		return new HashMap<>();
	}
}
