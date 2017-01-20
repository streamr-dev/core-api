package com.unifina.signalpath;

import java.util.Map;

/**
 * Adds noRepeat functionality to Output. When noRepeat is true, an event will only be sent
 * when the value of the output changes.
 * @param <T> type of the output
 */
public class PrimitiveOutput<T> extends Output<T> {

	public boolean noRepeat = false;
	public boolean canBeNoRepeat = true;

	public PrimitiveOutput(AbstractSignalPathModule owner, String name, String type) {
		super(owner, name, type);
	}

	@Override
	public void send(T value) {
		if (!noRepeat || previousValue == null || !value.equals(previousValue)) {
			super.send(value);
		}
	}

	@Override
	public Map<String,Object> getConfiguration() {
		Map<String,Object> config = super.getConfiguration();
		config.put("noRepeat",noRepeat);
		config.put("canBeNoRepeat",canBeNoRepeat);
		return config;
	}

	@Override
	public void setConfiguration(Map<String,Object> config) {
		super.setConfiguration(config);

		if (config.containsKey("noRepeat")) {
			noRepeat = Boolean.parseBoolean(config.get("noRepeat").toString());
		}
	}
}
