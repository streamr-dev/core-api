package com.unifina.signalpath.utils;

import com.unifina.signalpath.AbstractSignalPathModule;

import java.util.Map;

public class Comment extends AbstractSignalPathModule {

	private String text;

	@Override
	public void sendOutput() {}

	@Override
	public void clearState() {}

	@Override
	public Map<String, Object> getConfiguration() {
		Map<String, Object> config = super.getConfiguration();
		if (text != null) {
			config.put("text", text);
		}
		return config;
	}

	@Override
	protected void onConfiguration(Map<String, Object> config) {
		super.onConfiguration(config);
		if (config.containsKey("text")) {
			text = config.get("text").toString();
		}
	}
}
