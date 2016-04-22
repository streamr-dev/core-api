package com.unifina.signalpath;

import java.util.*;

public class MapInput extends Input<Map<String, Object>> {

	public MapInput(AbstractSignalPathModule owner, String name) {
		super(owner, name, "Map");
	}

	/**
	 * @return (shallow) copy of the input Map that can be freely modified
	 */
	public Map getModifiableValue() {
		return new LinkedHashMap(getValue());
	}
}
