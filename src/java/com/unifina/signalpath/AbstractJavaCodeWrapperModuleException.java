package com.unifina.signalpath;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AbstractJavaCodeWrapperModuleException extends ModuleException {
	public AbstractJavaCodeWrapperModuleException(String message, Throwable cause, List<ModuleExceptionMessage> moduleExceptions) {
		super(message, cause, moduleExceptions);
	}

	/**
	 * For front-end JSON output.
	 */
	@Override
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
