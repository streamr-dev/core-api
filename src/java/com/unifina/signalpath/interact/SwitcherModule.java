package com.unifina.signalpath.interact;

import com.unifina.signalpath.ModuleWithUI;
import com.unifina.signalpath.TimeSeriesOutput;

import java.util.Map;


public class SwitcherModule extends ModuleWithUI {

	TimeSeriesOutput out = new TimeSeriesOutput(this, "output");

	boolean value = false;

	@Override
	public void init() {
		addOutput(out);
	}

	@Override
	protected void onConfiguration(Map<String, Object> config) {
		super.onConfiguration(config);
		if(config.get("switcherValue") != null) {
			value = (boolean) config.get("switcherValue");
		}
	}

	@Override
	public Map<String, Object> getConfiguration() {
		Map<String, Object> config = super.getConfiguration();
		config.put("switcherValue", value);
		return config;
	}

	@Override
	public void connectionsReady() {
		super.connectionsReady();
		if(out.isConnected()) {
			if (value) {
				out.send(1);
			} else {
				out.send(0);
			}
		}
	}

	@Override
	public void sendOutput() {

	}

	@Override
	public void clearState() {
		value = false;
	}
}
