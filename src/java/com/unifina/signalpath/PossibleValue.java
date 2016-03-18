package com.unifina.signalpath;

import java.util.HashMap;

public class PossibleValue extends HashMap<String, Object> {

	// For serialization
	@SuppressWarnings("unused")
	public PossibleValue() {

	}

	public PossibleValue(String name, Object value) {
		put("name", name);
		put("value", value);
	}
}
