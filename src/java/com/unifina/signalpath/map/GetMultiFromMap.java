package com.unifina.signalpath.map;

import com.unifina.signalpath.*;
import com.unifina.signalpath.variadic.OutputInstantiator;
import com.unifina.signalpath.variadic.VariadicOutput;
import com.unifina.utils.MapTraversal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GetMultiFromMap extends AbstractSignalPathModule {

	private MapInput in = new MapInput(this, "in");

	private VariadicOutput<Object> outs = new VariadicOutput<>(this, 1, new OutputInstantiator.SimpleObject());
	private MapOutput founds = new MapOutput(this, "founds");


	@Override
	public void init() {
		addInput(in);
		addOutput(founds);
	}

	@Override
	public void sendOutput() {
		Map map = in.getValue();

		Map<String, Double> foundMap = new HashMap<>();

		for (Output<Object> out : outs.getEndpoints()) {
			String key = out.getEffectiveName();
			Object value = MapTraversal.getProperty(map, key);
			if (value == null) {
				foundMap.put(key, 0.0);
			} else {
				foundMap.put(key, 1.0);
				out.send(value);
			}
		}

		founds.send(foundMap);
	}

	@Override
	public void clearState() {}

	@Override
	public Map<String, Object> getConfiguration() {
		Map<String, Object> config = super.getConfiguration();
		outs.getConfiguration(config);
		return config;
	}

	@Override
	protected void onConfiguration(Map<String, Object> config) {
		super.onConfiguration(config);
		outs.onConfiguration(config);
	}
}
