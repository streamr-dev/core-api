package com.unifina.signalpath.map;

import com.unifina.signalpath.*;
import com.unifina.utils.MapTraversal;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GetMultiFromMap extends AbstractSignalPathModule {

	private MapInput in = new MapInput(this, "in");

	private List<Output<Object>> outs;
	private ListOutput founds = new ListOutput(this, "founds");


	@Override
	public void init() {
		addInput(in);
		addOutput(founds);
	}

	@Override
	public void sendOutput() {
		Map map = in.getValue();

		List<Double> foundList = new ArrayList<>(outs.size());

		for (Output<Object> out : outs) {
			String key = out.getDisplayName();
			Object value = MapTraversal.getProperty(map, key);
			if (value == null) {
				foundList.add(0.0);
			} else {
				foundList.add(1.0);
				out.send(value);
			}
		}

		founds.send(foundList);
	}

	@Override
	public void clearState() {}

	@Override
	public Map<String, Object> getConfiguration() {
		Map<String, Object> config = super.getConfiguration();

		ModuleOptions options = ModuleOptions.get(config);
		options.addIfMissing(ModuleOption.createInt("numOfKeys", outs.size()));

		return config;
	}

	@Override
	protected void onConfiguration(Map<String, Object> config) {
		super.onConfiguration(config);

		int numOfKeys = 1;

		ModuleOptions options = ModuleOptions.get(config);
		ModuleOption numOfKeysOption = options.getOption("numOfKeys");
		if (numOfKeysOption != null) {
			numOfKeys = numOfKeysOption.getInt();
		}

		outs = new ArrayList<>();

		for (int i=1; i <= numOfKeys; ++i) {
			Output<Object> out = new Output<>(this, "out-" + i, "Object");
			addOutput(out);
			outs.add(out);
		}
	}
}
