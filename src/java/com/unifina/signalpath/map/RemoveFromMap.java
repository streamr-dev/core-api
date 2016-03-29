package com.unifina.signalpath.map;

import com.unifina.signalpath.*;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class RemoveFromMap extends AbstractSignalPathModule {

	private MapInput in = new MapInput(this, "in");
	private StringInput key = new StringInput(this, "key");
	private MapOutput out = new MapOutput(this, "out");
	private Output<Object> removedItem = new Output<>(this, "item", "Object");

	@Override
	public void sendOutput() {
		Map map = in.getModifiableValue();
		Object r = map.remove(key.getValue());
		if (r != null) { removedItem.send(r); }
		out.send(map);
	}

	@Override
	public void clearState() {}
}
