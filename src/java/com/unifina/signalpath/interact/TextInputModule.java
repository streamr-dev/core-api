package com.unifina.signalpath.interact;

import com.unifina.signalpath.ModuleWithUI;
import com.unifina.signalpath.TimeSeriesOutput;

import java.util.Map;


public class TextInputModule extends ModuleWithUI {

	TimeSeriesOutput out = new TimeSeriesOutput(this, "output");

	String value = "";

	@Override
	public void init() {
		addOutput(out);
	}

	@Override
	protected void onConfiguration(Map<String, Object> config) {
		super.onConfiguration(config);

	}

	@Override
	public Map<String, Object> getConfiguration() {
		Map<String, Object> config = super.getConfiguration();

		return config;
	}

	@Override
	public void connectionsReady() {
		super.connectionsReady();

	}

	@Override
	public void sendOutput() {

	}

	@Override
	public void clearState() {
		value = "";
	}
}
