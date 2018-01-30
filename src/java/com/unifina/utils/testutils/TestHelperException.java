package com.unifina.utils.testutils;

public class TestHelperException extends RuntimeException {

	public TestHelperException(Exception exception, ModuleTestHelper testHelper) {
		super(exception.getMessage() + moduleStateAsString(testHelper), exception);
	}

	public TestHelperException(String message, ModuleTestHelper testHelper) {
		super(message + moduleStateAsString(testHelper));
	}

	private static String moduleStateAsString(ModuleTestHelper testHelper) {
		return " (clearState=" + testHelper.isClearStateCalled() +
				", serialized=" + testHelper.getSerializationMode() + ")";
	}
}
