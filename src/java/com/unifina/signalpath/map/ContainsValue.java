package com.unifina.signalpath.map;

import com.unifina.signalpath.*;

import java.util.Map;

public class ContainsValue extends AbstractSignalPathModule {
	private Input<Object> value = new Input<>(this, "value", "Object");
	private MapInput in = new MapInput(this, "in");
	private TimeSeriesOutput found = new TimeSeriesOutput(this, "found");

	@Override
	public void sendOutput() {
		Map map = in.getValue();
		found.send(map.containsValue(value.getValue()) ? 1.0 : 0.0);
	}

	@Override
	public void clearState() {}
}
