package com.unifina.signalpath.input;

import com.unifina.signalpath.*;

import java.util.HashMap;
import java.util.Map;


public class SwitcherModule extends InputModule {

	TimeSeriesOutput out = new TimeSeriesOutput(this, "output");

	boolean value = false;

	@Override
	public void init() {
		super.init();
		canClearState = false;
		resendAll = false;
		resendLast = 1;

		out.canBeNoRepeat = false;
	}

	@Override
	protected void onConfiguration(Map<String, Object> config) {
		super.onConfiguration(config);
		if(((Map)config.get("moduleData")) != null) {
			value = (boolean)((Map)config.get("moduleData")).get("value");
		}
	}

	@Override
	public Map<String, Object> getConfiguration() {
		Map<String, Object> config = super.getConfiguration();
		Map<String, Object> opt = new HashMap<>();
		opt.put("value", value);
		config.put("moduleOptions", opt);
		config.put("module", "StreamrSwitcher");
		return config;
	}

	@Override
	public void initialize() {
		for (Input i : out.getTargets()) {
			if (i instanceof TimeSeriesInput)
				((TimeSeriesInput)i).setInitialValue(value ? 1d : 0d);
			else if (i instanceof IntegerParameter)
				((IntegerParameter) i).receive(value ? 1 : 0);
			else i.receive(value ? 1d : 0d);
		}
	}

	@Override
	protected void onInput(RuntimeRequest request, RuntimeResponse response) {
		value = (boolean) request.get("value");
	}

	@Override
	public void sendOutput() {
		out.send(value ? 1d : 0d);
	}

	@Override
	public void clearState() {
		value = false;
	}
}
