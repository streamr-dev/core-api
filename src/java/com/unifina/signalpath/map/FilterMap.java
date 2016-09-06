package com.unifina.signalpath.map;

import com.unifina.signalpath.AbstractSignalPathModule;
import com.unifina.signalpath.ListParameter;
import com.unifina.signalpath.MapInput;
import com.unifina.signalpath.MapOutput;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class FilterMap extends AbstractSignalPathModule {
	private final ListParameter keys = new ListParameter(this, "keys");
	private final MapInput in = new MapInput(this, "in");
	private final MapOutput out = new MapOutput(this, "out");

	@Override
	public void sendOutput() {
		Map<String, Object> sourceMap = in.getValue();
		if (keys.getValue().isEmpty()) {
			out.send(sourceMap);
		} else {
			Map<String, Object> newMap = new LinkedHashMap<>();

			for (String key : (List<String>) keys.getValue()) {
				Object value = sourceMap.get(key);
				if (value != null) {
					newMap.put(key, value);
				}
			}

			out.send(newMap);
		}
	}

	@Override
	public void clearState() {}
}
