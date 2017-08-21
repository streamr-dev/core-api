package com.unifina.signalpath.input;

import com.unifina.signalpath.*;

import java.util.HashMap;
import java.util.Map;


public class SwitcherModule extends InputModule {

	BooleanOutput out = new BooleanOutput(this, "out");

	boolean value = false;

	@Override
	public void init() {
		super.init();
		canClearState = false;
		out.setCanBeNoRepeat(false);
	}

	@Override
	protected void onConfiguration(Map<String, Object> config) {
		super.onConfiguration(config);
		if (config.containsKey("switcherValue"))
			value = (boolean) config.get("switcherValue");
	}

	@Override
	public Map<String, Object> getConfiguration() {
		Map<String, Object> config = super.getConfiguration();
		config.put("switcherValue", value);
		return config;
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
	protected String getWidgetName() {
		return "StreamrSwitcher";
	}

	@Override
	public void initialize() {
		for (Input i : out.getTargets()) {
			if (i instanceof BooleanInput)
				((BooleanInput)i).setInitialValue(value);
			else i.receive(value);
		}
	}

	@Override
	protected void onInput(RuntimeRequest request, RuntimeResponse response) {
		value = (boolean) request.get("value");
	}

	@Override
	public void sendOutput() {
		out.send(value);
		updateUiState();
	}

	private void updateUiState() {
		Map<String,Object> msg = new HashMap<String,Object>();
		msg.put("switcherValue", value);
		pushToUiChannel(msg);
	}

	@Override
	public void clearState() {
		value = false;
		updateUiState();
	}
}
