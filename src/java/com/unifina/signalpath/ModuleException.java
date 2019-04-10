package com.unifina.signalpath;

import java.util.ArrayList;
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

	public static class CompileError extends HashMap<String, Object> {
		public CompileError(Long line, String message) {
			put("line", line);
			put("message", message);
		}
	}
	public List<CompileError> getCompilerErrors() {
		final List<CompileError> errors = new ArrayList<>();
		for (ModuleExceptionMessage msg : getModuleExceptions()) {
			for (Map map : msg.getErrors()) {
				Long line;
				try {
					line = Long.parseLong(map.get("line").toString());
				} catch (NumberFormatException e) {
					line = new Long(0);
				}
				final String message = map.get("msg").toString();
				final CompileError error = new CompileError(line, message);
				errors.add(error);
			}
		}
		return errors;
	}

}
