package com.unifina.signalpath.map;

import com.unifina.signalpath.*;
import com.unifina.signalpath.variadic.OutputInstantiator;
import com.unifina.signalpath.variadic.VariadicOutput;
import com.unifina.utils.MapTraversal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VariadicGetMultiFromMap extends AbstractSignalPathModule {

	private MapInput in = new MapInput(this, "in");
	private VariadicOutput<Object> outs = new VariadicOutput<>("in", this, new OutputInstantiator.SimpleObject(), 1);
	private MapOutput founds = new MapOutput(this, "founds");

	@Override
	public void init() {
		addInput(in);
		addVariadic(outs);
		addOutput(founds);
	}

	@Override
	public void sendOutput() {
		Map map = in.getValue();

		Map<String, Boolean> foundMap = new HashMap<>();


		for (Output<Object> out : outs.getEndpoints()) {
			String key = out.getEffectiveName();
			Object value = MapTraversal.getProperty(map, key);
			if (value == null) {
				foundMap.put(key, false);
			} else {
				foundMap.put(key, true);
				out.send(value);
			}
		}

		founds.send(foundMap);
	}

	@Override
	public void clearState() {}
}
