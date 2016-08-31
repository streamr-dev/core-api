package com.unifina.signalpath.map;

import com.mongodb.util.JSON;
import com.unifina.domain.signalpath.Canvas;
import com.unifina.service.SignalPathService;
import com.unifina.signalpath.*;

import java.util.*;

public class ForEach extends AbstractSignalPathModule {

	private SignalPathParameter signalPathParameter = new SignalPathParameter(this, "canvas");
	private StringInput key = new StringInput(this, "key");
	private StringOutput keyOut = new StringOutput(this, "key");
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
			input.getOwner().removeInput(input);
			input.setOwner(this);
			input.setJsClass(null); // Ensure variadic endpoints are imported as normal endpoints
			addInput(input);
			input.addProxiedInput(new InputTriggerTracker(this, triggeredInputs, input));
		}
		for (Output output : exportedOutputs) {
			output.getOwner().removeOutput(output);
			output.setOwner(this);
			output.setJsClass(null); // Ensure variadic endpoints are imported as normal endpoints
			addOutput(output);
			outputsToPropagate.add(output);
		}
	}

	// This module will activate on events arriving at its driving inputs,
	// like normal modules, but also on events originating from subcanvases!
	@Override
	public void sendOutput() {
		// Instantiate a new sub-canvas if not yet instantiated for received key
		String currentKey = key.getValue();
		if (!keyToSignalPath.containsKey(currentKey)) {
			SubSignalPath subSignalPath = makeSubSignalPath(this, currentKey);
			subSignalPath.proxyToOutputs(outputsToPropagate);
			cachedOutputValuesByKey.put(currentKey, instantiateCacheUpdateListeners(subSignalPath));
			keyToSignalPath.put(currentKey, subSignalPath);
		}

		SubSignalPath subSignalPath = keyToSignalPath.get(currentKey);

		// Pass along inputs to sub-canvas
		for (Input input : triggeredInputs) {
			subSignalPath.feedInput(input.getName(), input.getValue());
		}

		if (triggeredInputs.size() > 0) {
			subSignalPath.propagate();
		}

		// Other outputs sent indirectly via proxying
		map.send(cachedOutputValuesByKey);
		keyOut.send(currentKey);

		// Clear inputs for next module activation
		triggeredInputs.clear();
	}

	@Override
	public void clearState() {
		keyToSignalPath = new HashMap<>();
		cachedOutputValuesByKey = new HashMap<>();
	}

	private static SubSignalPath makeSubSignalPath(ForEach parent, String key) {
		SignalPathService signalPathService = parent.globals.getBean(SignalPathService.class);
		SignalPath signalPath = signalPathService.mapToSignalPath(parent.signalPathMap, false, parent.globals, false);
		signalPath.setParentSignalPath(parent.getParentSignalPath());

		Propagator propagator = new Propagator(signalPath.getExportedInputs(), parent);

		signalPath.connectionsReady();
		return new SubSignalPath(signalPath, propagator, key);
	}

	private Map<String, Object> instantiateCacheUpdateListeners(SubSignalPath subSignalPath) {
		Map<String, Object> outputCache = new HashMap<>();
		for (Output output : outputsToPropagate) {
			Input cacheUpdaterInput = new CacheUpdaterInput(this, subSignalPath.getKey(), output.getEffectiveName(), outputCache);
			// Needs to be added to this module, otherwise propagator dependency resolution won't be correct
			addInput(cacheUpdaterInput);
			subSignalPath.connectTo(output.getName(), cacheUpdaterInput);
		}
		return outputCache;
	}

	/**
	 * Solely used to keep track of inputs that received values (as opposed to having existing value).
	 */
	private static class InputTriggerTracker<T> extends Input<T> {

		private final Set<Input> triggeredInputs;
		private final Input originalInput;

		public InputTriggerTracker(AbstractSignalPathModule owner, Set<Input> triggeredInputs, Input originalInput) {
			super(owner, "ValueReceivedTracker-For-" + originalInput.getName(), "Object");
			this.triggeredInputs = triggeredInputs;
			this.originalInput = originalInput;
		}

		@Override
		public void receive(Object value) {
			triggeredInputs.add(originalInput);
		}
	}

	/**
	 * Connected to an <code>Output</code> so that it updates an entry of a <code>java.util.Map</code> every time a
	 * value is received.
	 */
	private static class CacheUpdaterInput<T> extends Input<T> {
		private final String outputName;
		private final Map<String, Object> cachedOutputValues;
		private final String key;

		public CacheUpdaterInput(AbstractSignalPathModule owner, String key, String outputName, Map<String, Object> cachedOutputValues) {
			super(owner, "CacheUpdaterInput-For-" + key + "-" + outputName, "Object");
			this.key = key;
			this.outputName = outputName;
			this.cachedOutputValues = cachedOutputValues;
		}

		@Override
		public void receive(Object value) {
			owner.setSendPending(true);
			owner.getInput("key").receive(key);
			cachedOutputValues.put(outputName, value);
		}

		// Always return true so that this input will never prevent ForEach from activating
		@Override
		public boolean isReady() {
			return true;
		}
	}
}
