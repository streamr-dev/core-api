package com.unifina.signalpath.map;

import com.unifina.signalpath.AbstractSignalPathModule;
import com.unifina.signalpath.MapInput;
import com.unifina.signalpath.MapOutput;
import com.unifina.signalpath.StringInput;

import java.util.Map;

public class RemoveFromMap extends AbstractSignalPathModule {

	private MapInput in = new MapInput(this, "in");
	private StringInput key = new StringInput(this, "key");
	private MapOutput out = new MapOutput(this, "out");

	@Override
	public void sendOutput() {
		Map map = in.getModifiableValue();
		map.remove(key.getValue());
		out.send(map);
	}

	@Override
	public void clearState() {}
}
