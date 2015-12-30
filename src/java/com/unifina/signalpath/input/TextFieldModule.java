package com.unifina.signalpath.input;

import com.unifina.signalpath.*;

import java.util.HashMap;
import java.util.Map;


public class TextFieldModule extends InputModule {

	StringOutput out = new StringOutput(this, "output");

	String value = "";

	@Override
	public void initialize() {
		for (Input i : out.getTargets()) {
			if(!value.equals(""))
				i.receive(value);
		}
	}

	@Override
	protected void onConfiguration(Map<String, Object> config) {
		super.onConfiguration(config);
		if((config.get("moduleData")) != null) {
			value = (String)((Map)config.get("moduleData")).get("value");
		}
	}

	@Override
	public Map<String, Object> getConfiguration() {
		Map<String, Object> config = super.getConfiguration();
		putToModuleOptions(config, "value", value);
		config.put("module", "StreamrTextField");
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


}
