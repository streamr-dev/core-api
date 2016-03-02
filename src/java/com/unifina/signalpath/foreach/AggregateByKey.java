package com.unifina.signalpath.foreach;

import com.unifina.signalpath.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public abstract class AggregateByKey extends AbstractSignalPathModule {

	private BooleanParameter sort = new BooleanParameter(this, "sort", false);
	private BooleanParameter ascending = new BooleanParameter(this, "ascending", false);
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

		addInput(sort);
		addInput(ascending);
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
		if (sort.getValue() && max > 0 && aggregateByKey.size() > max) {
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
		if (sort.getValue()) {
			return new ValueSortedMap<>(!ascending.getValue());
		} else {
			return new HashMap<>();
		}
	}

	@Override
	public void afterDeserialization() {
		super.afterDeserialization();

		// Ensure that proper Map type is used after de-serializing.
		Map<String, Double> oldAggregateByKey = aggregateByKey;
		aggregateByKey = initializeAggregateMap();
		aggregateByKey.putAll(oldAggregateByKey);
	}
}
