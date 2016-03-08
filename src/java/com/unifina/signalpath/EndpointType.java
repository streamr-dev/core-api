package com.unifina.signalpath;

import java.util.Arrays;
import java.util.List;

public enum EndpointType {
	NUMBER("number"),
	STRING("string"),
	BOOLEAN("boolean", "bool"),
	MAP("map"),
	LIST("list");

	private List<String> configNames;

	EndpointType(String... configName) {
		this.configNames = Arrays.asList(configName);
	}

	public static EndpointType fromConfigName(String configName) {
		for (EndpointType type : values()) {
			if (type.configNames.contains(configName)) {
				return type;
			}
		}
		throw new IllegalArgumentException("Value '" + configName +
			"' could not be mapped to " + EndpointType.class.getName() + ".");
	}

	public String toConfigName() {
		return configNames.get(0);
	}

	public Output instantiateOutput(AbstractSignalPathModule owner, String name) {
		if (this == NUMBER || this == BOOLEAN) {
			return new TimeSeriesOutput(owner, name);
		}
		if (this == STRING) {
			return new StringOutput(owner, name);
		}
		if (this == MAP) {
			return new MapOutput(owner, name);
		}
		if (this == LIST) {
			return new ListOutput(owner, name);
		}
		return null;
	}
}
