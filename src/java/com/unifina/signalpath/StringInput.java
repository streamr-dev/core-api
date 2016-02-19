package com.unifina.signalpath;

public class StringInput extends PrimitiveInput<String> {

	public StringInput(AbstractSignalPathModule owner, String name) {
		super(owner, name, "String");
	}

	@Override
	protected boolean validateInitialValue(String initialValue) {
		return !initialValue.equals("");
	}

	@Override
	protected String parseInitialValue(String initialValue) {
		return initialValue;
	}
}
