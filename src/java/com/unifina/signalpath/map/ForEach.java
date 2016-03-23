package com.unifina.signalpath.map;

import com.mongodb.util.JSON;
import com.unifina.domain.signalpath.Canvas;
import com.unifina.service.SignalPathService;
import com.unifina.signalpath.*;

import java.util.*;

public class ForEach extends AbstractSignalPathModule {

	private SignalPathParameter signalPathParameter = new SignalPathParameter(this, "canvas");
	private StringInput key = new StringInput(this, "key");
	private MapOutput map = new MapOutput(this, "map");

	private Map signalPathMap;
	private Map<String, SubSignalPath> keyToSignalPath = new HashMap<>();
	private Map<String, Map<String, Object>> cachedOutputValuesByKey = new HashMap<>();
	private List<Output> outputsToPropagate = new ArrayList<>();
	private final Set<Input> triggeredInputs = new HashSet<>();

	public ForEach() {
		super();
		signalPathParameter.setUpdateOnChange(true);
		canRefresh = true;
	}

	@Override
	protected void onConfiguration(Map<String, Object> config) {
		super.onConfiguration(config);

		// Load canvas
		Canvas canvas = signalPathParameter.getValue();
		if (canvas == null) {
			return;
		}

		// Construct temporary signal path so that endpoints can be analyzed
		signalPathMap = (Map) JSON.parse(canvas.getJson());
		SignalPathService signalPathService = globals.getBean(SignalPathService.class);
		SignalPath tempSignalPath = signalPathService.mapToSignalPath(signalPathMap, true, globals, false);

		// Find and validate exported endpoints
		List<Input> exportedInputs = tempSignalPath.getExportedInputs();
		List<Output> exportedOutputs = tempSignalPath.getExportedOutputs();
		if (exportedInputs.isEmpty()) {
			throw new RuntimeException("No exported inputs in canvas '" + canvas.getId() + "'. Need at least one.");
		}

		// Steal endpoints
		for (Input input : exportedInputs) {
			input.setOwner(this);
			addInput(input);
			input.addProxiedInput(new InputTriggerTracker(triggeredInputs, input));
		}
		for (Output output : exportedOutputs) {
			output.setOwner(this);
			addOutput(output);
			outputsToPropagate.add(output);
		}
	}

	@Override
	public void sendOutput() {
		// Instantiate a new sub-canvas if not yet instantiated for received key
		String currentKey = key.getValue();
		if (!keyToSignalPath.containsKey(currentKey)) {
			SubSignalPath subSignalPath = makeSubSignalPath(this);
			subSignalPath.proxyToOutputs(outputsToPropagate);
			cachedOutputValuesByKey.put(currentKey, instantiateCacheUpdateListeners(subSignalPath));
			keyToSignalPath.put(currentKey, subSignalPath);
		}

		SubSignalPath subSignalPath = keyToSignalPath.get(currentKey);

		// Pass along inputs to sub-canvas
		for (Input input : triggeredInputs) {
			subSignalPath.feedInput(input.getName(), input.getValue());
		}
		triggeredInputs.clear();

		subSignalPath.propagate();

		// Other outputs sent indirectly via proxying
		map.send(cachedOutputValuesByKey);
	}

	@Override
	public void clearState() {
		keyToSignalPath = new HashMap<>();
		cachedOutputValuesByKey = new HashMap<>();
	}

	private static SubSignalPath makeSubSignalPath(ForEach parent) {
		SignalPathService signalPathService = parent.globals.getBean(SignalPathService.class);
		SignalPath signalPath = signalPathService.mapToSignalPath(parent.signalPathMap, false, parent.globals, false);
		signalPath.setParentSignalPath(parent.getParentSignalPath());

		Propagator propagator = new Propagator(signalPath.getExportedInputs(), parent);

		signalPath.connectionsReady();
		return new SubSignalPath(signalPath, propagator);
	}

	private Map<String, Object> instantiateCacheUpdateListeners(SubSignalPath subSignalPath) {
		Map<String, Object> outputCache = new HashMap<>();
		for (Output output : outputsToPropagate) {
			String outputName = output.getName();
			subSignalPath.connectTo(outputName, new CacheUpdaterInput(outputName, outputCache));
		}
		return outputCache;
	}

	/**
	 * Solely used to keep track of inputs that received values (as opposed to having existing value).
	 */
	private static class InputTriggerTracker<T> extends Input<T> {

		private final Set<Input> triggeredInputs;
		private final Input originalInput;

		public InputTriggerTracker(Set<Input> triggeredInputs, Input originalInput) {
			super(null, "ValueReceivedTracker-For-" + originalInput.getName(), "Object");
			this.triggeredInputs = triggeredInputs;
			this.originalInput = originalInput;
		}

		@Override
		public void receive(Object value) {
			triggeredInputs.add(originalInput);
		}
	}

	/**
	 * Updates an entry of a <code>java.util.Map</code> every time a value is received.
	 */
	private static class CacheUpdaterInput<T> extends Input<T> {
		private final String outputName;
		private final Map<String, Object> cachedOutputValues;

		public CacheUpdaterInput(String outputName, Map<String, Object> cachedOutputValues) {
			super(null, "CacheUpdaterInput-For-" + outputName, "Object");
			this.outputName = outputName;
			this.cachedOutputValues = cachedOutputValues;
		}

		@Override
		public void receive(Object value) {
			cachedOutputValues.put(outputName, value);
		}
	}
}
