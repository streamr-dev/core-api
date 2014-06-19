package com.unifina.signalpath;


public class StringParameter extends Parameter<String> {
	
	public StringParameter(AbstractSignalPathModule owner, String name, String defaultValue) {
		super(owner, name, defaultValue, "String");
	}

	@Override
	public String parseValue(String s) {
		return s;
	}
	
	@Override
	protected boolean isEmpty(String value) {
		return super.isEmpty(value) || value.equals("");
	}

}
