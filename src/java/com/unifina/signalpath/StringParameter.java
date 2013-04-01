package com.unifina.signalpath;


public class StringParameter extends Parameter<String> {
	
	public StringParameter(AbstractSignalPathModule owner, String name, String defaultValue) {
		super(owner, name, defaultValue, "String");
	}

	@Override
	String parseValue(String s) {
		return s;
	}

}
