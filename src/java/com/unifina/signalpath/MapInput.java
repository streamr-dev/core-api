package com.unifina.signalpath;

import java.util.Map;

public class MapInput extends Input<Map<String, Object>> {

	public MapInput(AbstractSignalPathModule owner, String name) {
		super(owner, name, "Map");
	}

}
