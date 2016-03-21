package com.unifina.signalpath.map;

import com.unifina.signalpath.*;

import java.util.Map;

public class GetFromMap extends AbstractSignalPathModule {

	private StringParameter key = new StringParameter(this, "key", "id");
	private MapInput in = new MapInput(this, "in");
	private TimeSeriesOutput found = new TimeSeriesOutput(this, "found");
	private Output<Object> out = new Output<>(this, "out", "Object");

	@Override
	public void sendOutput() {
		Map source = in.getValue();
		Object target = source.get(key.getValue());
		if (target == null) {
			found.send(0.0);
		} else {
			found.send(1.0);
			out.send(target);
		}
	}

	@Override
	public void clearState() {}
}
