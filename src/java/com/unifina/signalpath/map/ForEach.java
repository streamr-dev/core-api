package com.unifina.signalpath.map;

import com.mongodb.util.JSON;
import com.unifina.domain.signalpath.Canvas;
import com.unifina.service.CanvasService;
import com.unifina.service.SerializationService;
import com.unifina.service.SignalPathService;
import com.unifina.signalpath.*;
import com.unifina.utils.Globals;
import com.unifina.utils.GlobalsFactory;
import grails.util.Holders;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.*;

public class ForEach extends AbstractSignalPathModule {

	private SignalPathParameter signalPathParameter = new SignalPathParameter(this, "canvas");
	private StringInput key = new StringInput(this, "key");
	private StringOutput keyOut = new StringOutput(this, "key");
	private MapOutput map = new MapOutput(this, "map");

	private Map<String, SubSignalPath> keyToSignalPath = new HashMap<>();
	private Map<String, Map<String, Object>> cachedOutputValuesByKey = new HashMap<>();
	private List<Output> outputsToPropagate = new ArrayList<>();
	private List<Input> stolenInputs = new ArrayList<>();
	private Set<Parameter> exportedUnconnectedParameters = new HashSet<>();
	private final Set<Input> triggeredInputs = new HashSet<>();

	public ForEach() {
		super();
		signalPathParameter.setUpdateOnChange(true);
		canRefresh = true;
	}

	@Override
	public Map<String, Object> getConfiguration() {
		Map<String, Object> config = super.getConfiguration();
		config.put("canvasKeys", keyToSignalPath.keySet());
		return config;
	}

	@Override
	protected void onConfiguration(Map<String, Object> config) {
		super.onConfiguration(config);

		// Load canvas
		Canvas canvas = signalPathParameter.getCanvas();
		if (canvas == null) {
			return;
		}

		// Construct temporary signal path so that endpoints can be analyzed
		// Note that the same map instance must not be reused for many instances, it will produce hell if many modules share the same config map instance
		Map signalPathMap = (Map) JSON.parse(canvas.getJson());
		SignalPathService signalPathService = Holders.getApplicationContext().getBean(SignalPathService.class);

		// Create a non-run-context Globals for instantiating the temporary SignalPath
		Globals tempGlobals = GlobalsFactory.createInstance(Collections.emptyMap(), getGlobals().getGrailsApplication(), getGlobals().getUser());
		SignalPath tempSignalPath = signalPathService.mapToSignalPath(signalPathMap, true, tempGlobals, new SignalPath(false));

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
			// Ensure variadic endpoints are imported as normal endpoints
			input.setJsClass(null);
			// Reset id for stolen endpoint to avoid clashes
			input.regenerateId();
			input.disconnect();
			addInput(input);
			input.addProxiedInput(new InputTriggerTracker(this, triggeredInputs, input));
			stolenInputs.add(input);
		}
		for (Output output : exportedOutputs) {
			output.getOwner().removeOutput(output);
			output.setOwner(this);
			output.setJsClass(null); // Ensure variadic endpoints are imported as normal endpoints
			// Reset id for stolen endpoint to avoid clashes
			output.regenerateId();
			output.disconnect();
			addOutput(output);
			outputsToPropagate.add(output);
		}
	}

	@Override
	public void initialize() {
		super.initialize();
		for (Input input : stolenInputs) {
			if (input instanceof Parameter && !input.isConnected()) {
				exportedUnconnectedParameters.add((Parameter) input);
			}
		}
	}

	// This module will activate on events arriving at its driving inputs,
	// like normal modules, but also on events originating from subcanvases!
	@Override
	public void sendOutput() {
		// Instantiate a new sub-canvas if not yet instantiated for received key
		String currentKey = key.getValue();
		if (!keyToSignalPath.containsKey(currentKey)) {
			SubSignalPath subSignalPath = makeSubSignalPath(currentKey);
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

	@Override
	public void setGlobals(Globals globals) {
		super.setGlobals(globals);

		// Also set on sub-SignalPaths
		for (String key : keyToSignalPath.keySet()) {
			SubSignalPath ssp = keyToSignalPath.get(key);
			ssp.getSignalPath().setGlobals(getGlobals());
		}
	}

	private SubSignalPath makeSubSignalPath(String key) {
		SignalPathService signalPathService = Holders.getApplicationContext().getBean(SignalPathService.class);
		CanvasService canvasService = Holders.getApplicationContext().getBean(CanvasService.class);

		// Note that the same map instance must not be reused for many instances, it will produce hell if many modules share the same config map instance
		// Re-parsing is used here instead of deep-copying an already-parsed map, as that's not easily available
		Map signalPathMap = (Map) JSON.parse(signalPathParameter.getCanvas().getJson());
		canvasService.resetUiChannels(signalPathMap);
		SignalPathInsideForEach signalPath = (SignalPathInsideForEach) signalPathService.mapToSignalPath(signalPathMap, false, getGlobals(), new SignalPathInsideForEach(false));
		signalPath.setName(signalPath.getName() + " (" + key + ")");
		signalPath.link(key, this);

		// Copy parameter default values
		for (Parameter p : exportedUnconnectedParameters) {
			Parameter p2 = (Parameter) signalPath.getInput(p.getName());
			p2.setDefaultValue(p.getValue());

			// Runtime changes are received
			p.addProxiedInput(p2);
		}

		signalPath.setParentSignalPath(getParentSignalPath());

		Propagator propagator = new Propagator(signalPath.getExportedInputs());

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

	@Override
	public AbstractSignalPathModule resolveRuntimeRequestRecipient(RuntimeRequest request, RuntimeRequest.PathReader path) {
		if (path.isEmpty()) {
			return super.resolveRuntimeRequestRecipient(request, path);
		} else {
			String key = path.readString("keys");
			// URLDecode twice, see forEachModule.js
			try {
				key = URLDecoder.decode(URLDecoder.decode(key, "UTF-8"), "UTF-8");
			} catch (UnsupportedEncodingException e) {}

			SubSignalPath ssp = keyToSignalPath.get(key);
			if (ssp == null) {
				throw new IllegalArgumentException("Subcanvas not found: "+key);
			} else {
				return ssp.getSignalPath().resolveRuntimeRequestRecipient(request, path);
			}
		}
	}

	public SignalPath getSignalPathByKey(String key) {
		SubSignalPath sub = keyToSignalPath.get(key);
		if (sub == null) {
			return null;
		} else {
			return sub.getSignalPath();
		}
	}

	@Override
	public void beforeSerialization() {
		super.beforeSerialization();
		for (SubSignalPath subSignalPath : keyToSignalPath.values()) {
			subSignalPath.getSignalPath().beforeSerialization();
		}
	}

	@Override
	public void afterSerialization() {
		super.afterSerialization();
		for (SubSignalPath subSignalPath : keyToSignalPath.values()) {
			subSignalPath.getSignalPath().afterSerialization();
		}
	}

	@Override
	public void afterDeserialization(SerializationService serializationService) {
		super.afterDeserialization(serializationService);
		for (SubSignalPath subSignalPath : keyToSignalPath.values()) {
			subSignalPath.getSignalPath().afterDeserialization(serializationService);
		}
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
			getOwner().setSendPending(true);
			getOwner().getInput("key").receive(key);
			cachedOutputValues.put(outputName, value);
		}

		// Always return true so that this input will never prevent ForEach from activating
		@Override
		public boolean isReady() {
			return true;
		}
	}

	public static class SignalPathInsideForEach extends SignalPath {

		ForEach parent;
		String key;

		public SignalPathInsideForEach(boolean isRoot) {
			super(isRoot);
		}

		public void link(String key, ForEach parent) {
			this.key = key;
			this.parent = parent;
		}

		@Override
		protected RuntimeRequest.PathWriter getRuntimePath(RuntimeRequest.PathWriter writer) {
			return parent.getRuntimePath(writer).write("/keys/"+key);
		}
	}
}
