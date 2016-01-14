package com.unifina.signalpath.input;

import com.unifina.signalpath.*;

import java.util.HashMap;
import java.util.Map;


public class TextFieldModule extends InputModule {

	StringOutput out = new StringOutput(this, "out");

	String value = "";

	@Override
	public void initialize() {
		resendAll = false;
		resendLast = 1;

		for (Input i : out.getTargets()) {
			if(!value.equals(""))
				i.receive(value);
		}
	}

	@Override
	protected void onConfiguration(Map<String, Object> config) {
		super.onConfiguration(config);
		if (config.containsKey("textFieldValue")) {
			value = (String) config.get("textFieldValue");
		}
	}

	@Override
	public Map<String, Object> getConfiguration() {
		Map<String, Object> config = super.getConfiguration();
		config.put("textFieldValue", value);
		return config;
	}

	@Override
	protected void onInput(RuntimeRequest request, RuntimeResponse response) {
		value = (String) request.get("value");
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
