package com.unifina.signalpath;

import com.unifina.exceptions.ModuleExceptionMessage;

public class CyclicCanvasModuleExceptionMessage extends ModuleExceptionMessage {
	public CyclicCanvasModuleExceptionMessage(int moduleId, String message) {
		super(moduleId, message);
	}
}
