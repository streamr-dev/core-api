package com.unifina.signalpath.map;

import com.unifina.signalpath.*;

import java.util.Map;

public class PutToMap extends AbstractSignalPathModule {

	private MapInput mapInput = new MapInput(this, "map");
	private StringInput key = new StringInput(this, "key");
	private Input<Object> value = new Input<>(this, "value", "Object");

	private MapOutput mapOutput = new MapOutput(this, "map");

	@Override
	public void sendOutput() {
		Map map = mapInput.getModifiableValue();
		map.put(key.getValue(), value.getValue());
		mapOutput.send(map);
	}

	@Override
	public void clearState() {

	}
}
