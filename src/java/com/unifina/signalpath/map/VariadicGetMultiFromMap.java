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
	private VariadicOutput<Object> outs = new VariadicOutput<>(this, new OutputInstantiator.SimpleObject(), 1);
	private MapOutput founds = new MapOutput(this, "founds");


	@Override
	public void init() {
		addInput(in);
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

	@Override
	public Output getOutput(String name) {
		Output output = super.getOutput(name);
		if (output == null) {
			output = outs.addEndpoint(name);
		}
		return output;
	}

	@Override
	protected void onConfiguration(Map<String, Object> config) {
		super.onConfiguration(config);
		outs.onConfiguration(config);
	}
}
