package com.unifina.signalpath.map;

import com.unifina.signalpath.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public abstract class AggregateByKey extends AbstractSignalPathModule {

	private boolean sorted = false;
	private boolean ascending = false;

	private IntegerParameter maxKeyCount = new IntegerParameter(this, "maxKeyCount", 0);
	private StringInput key = new StringInput(this, "key");

	private MapOutput map = new MapOutput(this, "map");
	private TimeSeriesOutput valueOfCurrentKey = new TimeSeriesOutput(this, "valueOfCurrentKey");

	private Map<String, Double> aggregateByKey;

	/**
	 * Handle received key (along with current aggregate value), and return new aggregate value.
	 */
	protected abstract double onSendOutput(String key, Double currentValue);

	/**
	 * Handle removal of key
	 */
	protected abstract void onKeyDropped(String key, Double lastValue);

	@Override
	public void init() {
		super.init();

		addInput(maxKeyCount);
		addInput(key);
		addOutput(map);
		addOutput(valueOfCurrentKey);
	}

	@Override
	public void sendOutput() {
		if (aggregateByKey == null) {
			aggregateByKey = initializeAggregateMap();
		}

		double newValue = onSendOutput(key.getValue(), aggregateByKey.get(key.getValue()));
		aggregateByKey.put(key.getValue(), newValue);

		pruneTreeIfNeeded();

		map.send(Collections.unmodifiableMap(aggregateByKey));
		valueOfCurrentKey.send(newValue);
	}

	private void pruneTreeIfNeeded() {
		int max = maxKeyCount.getValue();
		if (sorted && max > 0 && aggregateByKey.size() > max) {
			String keyToRemove = ((TreeMap<String, Double>) aggregateByKey).lastKey();
			Double value = aggregateByKey.remove(keyToRemove);
			onKeyDropped(keyToRemove, value);
		}
	}

	@Override
	public void clearState() {
		aggregateByKey = initializeAggregateMap();
	}

	private Map<String, Double> initializeAggregateMap() {
		if (sorted) {
			return new ValueSortedMap<>(!ascending);
		} else {
			return new HashMap<>();
		}
	}

	@Override
	public Map<String, Object> getConfiguration() {
		Map<String,Object> config = super.getConfiguration();

		ModuleOptions options = ModuleOptions.get(config);
		options.addIfMissing(ModuleOption.createBoolean("sorted", sorted));
		options.addIfMissing(ModuleOption.createBoolean("ascending", ascending));

		return config;
	}

	@Override
	public void configure(Map<String, Object> config) {
		super.configure(config);

		ModuleOptions options = ModuleOptions.get(config);
		if (options.containsKey("sorted")) {
			sorted = options.getOption("sorted").getBoolean();
		}
		if (options.containsKey("ascending")) {
			ascending = options.getOption("ascending").getBoolean();
		}
	}
}
