package com.unifina.utils.testutils;

import com.unifina.serialization.HiddenFieldDetector;

public class TestHelperException extends RuntimeException {

	public TestHelperException(Exception exception, ModuleTestHelper testHelper) {
		super(exception.getMessage() + moduleStateAsString(testHelper), exception);
	}

	public TestHelperException(String message, ModuleTestHelper testHelper) {
		super(message + moduleStateAsString(testHelper));
	}

	public TestHelperException(HiddenFieldDetector hiddenFieldDetector) {
		super("Field shadowing not allowed because of serialization issues. Problem: " +
				hiddenFieldDetector.hiddenFields().toString());
	}

	private static String moduleStateAsString(ModuleTestHelper testHelper) {
		return " (clearState=" + testHelper.isClearStateCalled() +
				", serialized=" + testHelper.getSerializationMode() + ")";
	}
}
