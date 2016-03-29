package com.unifina.signalpath;

import java.util.*;

public class MapInput extends Input<Map<String, Object>> {

	public MapInput(AbstractSignalPathModule owner, String name) {
		super(owner, name, "Map");
	}

	public Map getModifiableValue() {
		Map m = getValue();
		return canBeModifiedAsSuch(m) ? m : new LinkedHashMap(m);
	}

	private boolean canBeModifiedAsSuch(Map m) {
		return m instanceof HashMap || m instanceof TreeMap || m instanceof Hashtable;
	}

}
