package com.unifina.signalpath;

import java.util.Collections;
import java.util.Map;
import java.util.SortedMap;

public class MapOutput extends Output<Map> {

	public MapOutput(AbstractSignalPathModule owner, String name) {
		super(owner, name, "Map");
	}

	@Override
	public void send(Map value) {

		// prevent modification of sent Maps (no copying, just overriding modifying methods)
		//   if a module wants to modify the value, it must make a personal copy
		// see: MapInput.getModifiableValue
		// if T == ConcurrentMap, this breaks
		//   for now, we only have T \in {Object, Map}
		//   if in future we need e.g. ConcurrentMapInput/Output, this needs to be changed, too
		if (value instanceof SortedMap) {
			value = Collections.unmodifiableSortedMap((SortedMap) value);
		} else {
			value = Collections.unmodifiableMap(value);
		}

		super.send(value);
	}
}
