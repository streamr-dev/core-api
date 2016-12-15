package com.unifina.signalpath.input;

import com.unifina.signalpath.*;

import java.util.HashMap;
import java.util.Map;


public class TextFieldModule extends InputModule {

	StringOutput out = new StringOutput(this, "out");

	String value = "";
	Integer width = null;
	Integer height = null;

	@Override
	public void initialize() {
		for (Input i : out.getTargets()) {
			if (!value.equals(""))
				i.receive(value);
		}
	}

	@Override
	protected void handleRequest(RuntimeRequest request, RuntimeResponse response) {
		super.handleRequest(request, response);
		if (request.getType().equals("getState")) {
			response.put("state", value);
			response.setSuccess(true);
		}
	}

	@Override
	protected void onConfiguration(Map<String, Object> config) {
		super.onConfiguration(config);
		if (config.containsKey("textFieldValue")) {
			value = (String) config.get("textFieldValue");
		}
		if(config.containsKey("textFieldWidth")) {
			width = (Integer) config.get("textFieldWidth");
		}
		if(config.containsKey("textFieldHeight")) {
			height = (Integer) config.get("textFieldHeight");
		}
	}

	@Override
	public Map<String, Object> getConfiguration() {
		Map<String, Object> config = super.getConfiguration();
		config.put("textFieldValue", value);
		if(width != null)
			config.put("textFieldWidth", width);
		if(height != null)
			config.put("textFieldHeight", height);
		return config;
	}

	@Override
	protected void onInput(RuntimeRequest request, RuntimeResponse response) {
		value = (String)request.get("value");
	}

	@Override
	public void sendOutput() {
		out.send(value);
	}

	@Override
	public void clearState() {
		value = "";
	}

	@Override
	protected String getWidgetName() {
		return "StreamrTextField";
	}
}
