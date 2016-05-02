package com.unifina.signalpath.map;

import com.unifina.signalpath.*;
import com.unifina.utils.window.WindowListener;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public abstract class AggregateByKey extends AbstractModuleWithWindow<Double> {

	private boolean sorted = false;
	private boolean ascending = false;
	private boolean deleteEmptyKeys = true;

	private IntegerParameter maxKeyCount = new IntegerParameter(this, "maxKeyCount", 0);
	private StringInput key = new StringInput(this, "key");

	private MapOutput map = new MapOutput(this, "map");

	protected Map<String, Double> aggregateByKey;

	/**
	 * Should return the value that gets added to the window on this event
	 */
	protected abstract Double getNewWindowValue();

	@Override
	public void init() {
		supportsMinSamples = false;
		super.init();
	}

	@Override
	protected void handleInputValues() {
		if (aggregateByKey == null) {
			aggregateByKey = initializeAggregateMap();
		}
		addToWindow(getNewWindowValue(), key.getValue());
	}

	@Override
	protected void doSendOutput() {
		pruneTreeIfNeeded();
		map.send(Collections.unmodifiableMap(aggregateByKey));
	}

	private void pruneTreeIfNeeded() {
		int max = maxKeyCount.getValue();
		if (sorted && max > 0 && aggregateByKey.size() > max) {
			String keyToRemove = ((TreeMap<String, Double>) aggregateByKey).lastKey();
			aggregateByKey.remove(keyToRemove);
			deleteWindow(keyToRemove);
		}
	}

	@Override
	public void clearState() {
		super.clearState();
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
		options.addIfMissing(ModuleOption.createBoolean("sort", sorted));

		ModuleOption sortOrder = ModuleOption.createString("sortOrder", ascending ? "ascending" : "descending");
		sortOrder.addPossibleValue("ascending", "ascending");
		sortOrder.addPossibleValue("descending", "descending");
		options.addIfMissing(sortOrder);

		options.addIfMissing(ModuleOption.createBoolean("deleteEmptyKeys", deleteEmptyKeys));

		return config;
	}

	@Override
	public void onConfiguration(Map<String, Object> config) {
		super.onConfiguration(config);

		ModuleOptions options = ModuleOptions.get(config);
		if (options.containsKey("sort")) {
			sorted = options.getOption("sort").getBoolean();
		}
		if (options.containsKey("sortOrder")) {
			ascending = options.getOption("sortOrder").getString().equals("ascending");
		}
		if (options.containsKey("deleteEmptyKeys")) {
			deleteEmptyKeys = options.getOption("deleteEmptyKeys").getBoolean();
		}
	}

	protected void increment(String key, Double by) {
		Double currentValue = aggregateByKey.get(key);
		aggregateByKey.put(key, currentValue == null ? by : currentValue + by);
	}

	protected void decrement(String key, Double by) {
		increment(key, -by);
	}

	/**
	 * Returns a AggregateByKeyWindowListener, which increments and decrements
	 * aggregates as values are added and removed to the key-specific window.
	 * For custom behavior, override this method and return your custom
	 * window listener.
     */
	@Override
	protected WindowListener<Double> createWindowListener(Object key) {
		return new AggregateByKeyWindowListener(key.toString());
	}

	class AggregateByKeyWindowListener implements WindowListener<Double> {

		private final String key;

		public AggregateByKeyWindowListener(String key) {
			this.key = key;
		}

		@Override
		public void onAdd(Double item) {
			increment(key, item);
		}

		@Override
		public void onRemove(Double item) {
			decrement(key, item);

			if (deleteEmptyKeys && windowByKey.get(key).getSize()==0) {
				deleteWindow(key);
				aggregateByKey.remove(key);
			}
		}

		@Override
		public void onClear() {

		}
	}
}
