package com.unifina.signalpath;

import com.unifina.exceptions.ModuleExceptionMessage;

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

	/**
	 * For front-end JSON output.
	 */
	public Map<String, Object> toMap() {
		List<Map<String, Object>> list = new ArrayList<>();
		for (ModuleExceptionMessage e : getModuleExceptions()) {
			list.add(e.toMap());
		}
		Map<String, Object> result = new HashMap<>();
		result.put("errors", list);
		return result;
	}
}
