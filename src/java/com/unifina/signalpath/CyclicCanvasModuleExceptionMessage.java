package com.unifina.signalpath;

import com.unifina.signalpath.custom.ModuleExceptionMessage;

public class CyclicCanvasModuleExceptionMessage extends ModuleExceptionMessage {
	public CyclicCanvasModuleExceptionMessage(int moduleId, String message) {
		super(moduleId, message);
	}
}
