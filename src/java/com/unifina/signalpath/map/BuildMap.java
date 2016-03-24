package com.unifina.signalpath.map;

import com.unifina.signalpath.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BuildMap extends AbstractSignalPathModule {
	private MapOutput map = new MapOutput(this, "map");

	private List<Input<Object>> ins;

	@Override
	public void init() {
		addOutput(map);
	}

	@Override
	public void sendOutput() {
		Map<String, Object> ret = new HashMap<>();

		for (Input<Object> in : ins) {
			String key = in.getEffectiveName();
			Object value = in.getValue();
			ret.put(key, value);
		}

		map.send(ret);
	}

	@Override
	public void clearState() {}

	@Override
	public Map<String, Object> getConfiguration() {
		Map<String, Object> config = super.getConfiguration();

		ModuleOptions options = ModuleOptions.get(config);
		options.addIfMissing(ModuleOption.createInt("numOfKeys", ins.size()));

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

		ins = new ArrayList<>();

		for (int i=1; i <= numOfKeys; ++i) {
			Input<Object> in = new Input<>(this, "in-" + i, "Object");
			addInput(in);
			ins.add(in);
		}
	}
}
