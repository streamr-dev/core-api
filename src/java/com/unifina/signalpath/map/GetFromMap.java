package com.unifina.signalpath.map;

import com.unifina.signalpath.*;
import com.unifina.utils.MapTraversal;

import java.util.Map;

public class GetFromMap extends AbstractSignalPathModule {

	private StringParameter key = new StringParameter(this, "key", "id");
	private MapInput in = new MapInput(this, "in");
	private BooleanOutput found = new BooleanOutput(this, "found");
	private Output<Object> out = new Output<>(this, "out", "Object");

	@Override
	public void sendOutput() {
		Map source = in.getValue();
		Object target = MapTraversal.getProperty(source, key.getValue());
		found.send(target != null);
		if (target != null) {
			out.send(target);
		}
	}

	@Override
	public void clearState() {}
}
