package com.unifina.signalpath.map;

import com.unifina.signalpath.*;

import java.util.Map;

public class ContainsValue extends AbstractSignalPathModule {
	private Input<Object> value = new Input<>(this, "value", "Object");
	private MapInput in = new MapInput(this, "in");
	private BooleanOutput found = new BooleanOutput(this, "found");

	@Override
	public void sendOutput() {
		Map map = in.getValue();
		found.send(map.containsValue(value.getValue()));
	}

	@Override
	public void clearState() {}
}
