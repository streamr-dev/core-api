package com.unifina.signalpath;


import java.util.Map;

public class StringParameter extends Parameter<String> {

	private Boolean isTextArea = false;

	public StringParameter(AbstractSignalPathModule owner, String name, String defaultValue) {
		super(owner, name, defaultValue, "String");
	}

	@Override
	public Map<String, Object> getConfiguration() {
		Map<String, Object> conf = super.getConfiguration();
		conf.put("isTextArea", isTextArea);
		return conf;
	}

	@Override
	public void setConfiguration(Map<String, Object> config) {
		super.setConfiguration(config);
		if (config.containsKey("isTextArea")) {
			isTextArea = (Boolean)config.get("isTextArea");
		}
	}

	@Override
	public String parseValue(String s) {
		return s;
	}
	
	@Override
	protected boolean isEmpty(String value) {
		return super.isEmpty(value) || value.equals("");
	}

	public boolean isTextArea() {
		return isTextArea;
	}

	public void setTextArea(boolean textArea) {
		isTextArea = textArea;
	}
}
