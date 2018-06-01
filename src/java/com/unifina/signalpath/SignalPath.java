package com.unifina.signalpath;

import com.unifina.data.FeedEvent;
import com.unifina.domain.data.Stream;
import com.unifina.domain.signalpath.Canvas;
import com.unifina.domain.signalpath.Module;
import com.unifina.serialization.SerializationRequest;
import com.unifina.service.CanvasService;
import com.unifina.service.ModuleService;
import com.unifina.service.SerializationService;
import com.unifina.utils.Globals;
import grails.converters.JSON;
import grails.util.Holders;
import org.apache.log4j.Logger;
import org.codehaus.groovy.grails.web.json.JSONObject;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.*;

import static java.util.stream.Collectors.toSet;

public class SignalPath extends ModuleWithUI {

	private static final Logger log = Logger.getLogger(SignalPath.class);
	private static Comparator<AbstractSignalPathModule> initPriorityComparator = new Comparator<AbstractSignalPathModule>() {
		@Override
		public int compare(AbstractSignalPathModule o1, AbstractSignalPathModule o2) {
			return Integer.compare(o1.getInitPriority(), o2.getInitPriority());
		}
	};

	private SignalPathParameter signalPathParameter;

	private List<AbstractSignalPathModule> mods = new ArrayList<>();

	private List<Input> exportedInputs = new ArrayList<Input>();
	private List<Output> exportedOutputs = new ArrayList<Output>();

	private Canvas canvas = null;
	private Map<Integer, AbstractSignalPathModule> modulesByHash = new HashMap<>();

	private boolean root = false;
	private SignalPath cachedRootSignalPath = null;

	public SignalPath() {
		super();
		setInitPriority(10);
		canRefresh = true;
	}

	public SignalPath(boolean isRoot) {
		this();
		this.root = isRoot;
	}

	@Deprecated
	public SignalPath(Map iData, boolean isRoot, Globals globals) {
		super();
		this.root = isRoot;
		this.setGlobals(globals);
		setInitPriority(10);
		canRefresh = true;

		// Backwards compatibility, TODO: remove this constructor eventually
		initFromRepresentation(iData);
	}

	public boolean isRoot() {
		return root;
	}

	// TODO: remove backwards compatibility eventually
	@Override
	public Input getInput(String name) {
		if (name.equals("signalPath") || name.equals("streamlet")) { name = "canvas"; }
		return super.getInput(name);
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	private void initFromRepresentation(Map iData) {
		if (iData.get("name") != null) {
			setName(iData.get("name").toString());
		}

		List<InputConnection> inputs = new ArrayList<>();
		Map<String, Output> outputs = new HashMap<>();

		List<Map> modulesJSON = (List<Map>) iData.get("modules");
		if (modulesJSON == null) {
			modulesJSON = new ArrayList<>(0);
		}

		if (modulesJSON.isEmpty()) {
			return;
		}

		ModuleService moduleService = Holders.getApplicationContext().getBean(ModuleService.class);

		HashMap<Long, Module> moduleDomainById = new HashMap<>();
		for (Module m : moduleService.getModuleDomainObjects(modulesJSON)) {
			moduleDomainById.put(m.getId(), m);
		}

		List<ModuleConfig> moduleConfigs = new ArrayList<>();
		for (Map moduleConfig : modulesJSON) {

			Module moduleDomain = moduleDomainById.get(((Number) moduleConfig.get("id")).longValue());

			try {
				AbstractSignalPathModule moduleImpl = moduleService.getModuleInstance(moduleDomain, moduleConfig, this, getGlobals());

				// Get parameter inputs and outputs if connected, or create constants if not connected
				for (Map paramConfig : (List<Map>) moduleConfig.get("params")) {
					Input i = moduleImpl.getInput(paramConfig.get("name").toString());

					if (i == null) {
						log.warn("No such parameter: " + paramConfig.get("name"));
					} else {
						if (paramConfig.containsKey("connected") && (boolean) paramConfig.get("connected")) {
							inputs.add(new InputConnection(i, paramConfig.get("sourceId").toString()));
						} else {
							// Param value has already been set in configuration. Should something more be done?
						}

						if (paramConfig.containsKey("export") && (boolean) paramConfig.get("export")) {
							exportedInputs.add(i);
						}
					}
				}

				moduleConfigs.add(new ModuleConfig(moduleImpl, moduleConfig));
				mods.add(moduleImpl);

			} catch (ModuleCreationFailedException e) {
				log.warn("Failed to instantiate and configure module " + moduleConfig.get("id"));
			}
		}

		// Collect inputs and outputs with endpoints from modules
		for (ModuleConfig mc : moduleConfigs) {
			AbstractSignalPathModule mod = mc.module;

			for (Map inputConfig : (List<Map>) mc.config.get("inputs")) {
				Input i = mod.getInput(inputConfig.get("name").toString());
				if (i == null) {
					log.warn("No such input: " + inputConfig.get("name"));
				} else {
					if (inputConfig.containsKey("connected") && (boolean) inputConfig.get("connected")) {
						inputs.add(new InputConnection(i, inputConfig.get("sourceId").toString()));
					}
					if (inputConfig.containsKey("export") && (boolean) inputConfig.get("export")) {
						exportedInputs.add(i);
					}
				}
			}

			for (Map outputConfig : (List<Map>) mc.config.get("outputs")) {
				Output o = mod.getOutput(outputConfig.get("name").toString());
				if (o == null) {
					log.warn("No such output: " + outputConfig.get("name"));
				} else {
					if (outputConfig.containsKey("connected") && (boolean) outputConfig.get("connected") && outputConfig.get("id") != null) {
						outputs.put(outputConfig.get("id").toString(), o);
					}
					if (outputConfig.containsKey("export") && (boolean) outputConfig.get("export")) {
						exportedOutputs.add(o);
					}
				}
			}

		}

		// Wire every connected input to its source output
		for (InputConnection ic : inputs) {
			Output o = outputs.get(ic.connectedTo);
			if (o != null) {
				o.connect(ic.input);
			} else {
				log.warn("Input " + ic.input.getName() + " could not be connected, because source was not found. SP: " + ic.input.getOwner().getParentSignalPath() + ", TOP: " + ic.input.getOwner().getRootSignalPath());
			}
		}

		// Exported inputs may be exported with another name
		// If the io names in different modules clash, add a running number in the end to make the name unique
		if (!root) {
			for (Input it : exportedInputs) {
				// Ensure variadic endpoints are imported as normal endpoints
				it.setJsClass(null);
				// Prevent exported Endpoints from being exported on wrapping module
				it.setExport(false);
				// Id needs to be regenerated to avoid clashes with other instances of the same canvas-as-a-module
				it.regenerateId();
				if (getInput(it.getName()) == null) {
					addInput(it);
				}
				else {
					int counter = 2;
					while (getInput(it.getName() + counter) != null) {
						counter++;
					}
					it.setName(it.getName() + counter);
					addInput(it);
				}
			}
			for (Output it : exportedOutputs) {
				// Ensure variadic endpoints are imported as normal endpoints
				it.setJsClass(null);
				// Prevent exported Endpoints from being exported on wrapping module
				it.setExport(false);
				// Id needs to be regenerated to avoid clashes with other instances of the same canvas-as-a-module
				it.regenerateId();
				if (getOutput(it.getName()) == null) {
					addOutput(it);
				}
				else {
					int counter = 2;
					while (getOutput(it.getName() + counter) != null) {
						counter++;
					}
					it.setName(it.getName() + counter);
					addOutput(it);
				}
			}
		}
	}

	public List<AbstractSignalPathModule> getModules() {
		return mods;
	}

	public boolean hasExports() {
		return exportedInputs.size() + exportedOutputs.size() > 0;
	}

	public boolean isSerializable() {
		return !getGlobals().isAdhoc() && getGlobals().isSerializationEnabled() && isRoot();
	}

	@Override
	public Map<String, Object> getConfiguration() {
		Map<String, Object> config = super.getConfiguration();
		List<Map> modules = new ArrayList<>(mods.size());
		for (AbstractSignalPathModule m : mods) {
			modules.add(m.getConfiguration());
		}
		config.put("modules", modules);
		return config;
	}

	@Override
	public void onConfiguration(Map config) {
		super.onConfiguration(config);
		if (!root && signalPathParameter.hasValue()) {
			/**
			 * Reset uiChannels if this is a subcanvas (not the root canvas). Otherwise
			 * there will be problems if many instances of the same canvas are used
			 * as subcanvases, as all the instances would produce to same uiChannels.
			 */
			Map json = (JSONObject) JSON.parse(signalPathParameter.getCanvas().getJson());
			CanvasService canvasService = Holders.getApplicationContext().getBean(CanvasService.class);
			canvasService.resetUiChannels(json);
			initFromRepresentation(json);
		} else {
			initFromRepresentation(config);
		}
	}

	@Override
	public void connectionsReady() {
		super.connectionsReady();

		List<AbstractSignalPathModule> sortedModules = new ArrayList<>();
		for (AbstractSignalPathModule module : getModules()) {
			modulesByHash.put(module.getHash(), module);
			sortedModules.add(module);
		}

		// Call connectionsReady in modules in ascending init priority order
		Collections.sort(sortedModules, initPriorityComparator);

		for (int i = 0; i < sortedModules.size(); i++) {
			sortedModules.get(i).connectionsReady();
		}

		if (getGlobals().getDataSource() != null) {
			getGlobals().getDataSource().connectSignalPath(this);
		}
	}

	@Override
	public void sendOutput() {

	}

	@Override
	public void clearState() {
		for (AbstractSignalPathModule module : getModules()) {
			module.clear();
		}
	}

	@Override
	public void init() {
		if (!root) {
			signalPathParameter = new SignalPathParameter(this, "canvas");
			signalPathParameter.setUpdateOnChange(true);
			addInput(signalPathParameter);
		}
	}

	@Override
	public void receive(FeedEvent event) {
		if (event.content instanceof SerializationRequest) {
			((SerializationRequest) event.content).serialize(this);
		} else {
			super.receive(event);
		}
	}

	@Override
	public void destroy() {
		super.destroy();

		for (AbstractSignalPathModule it : mods) {
			// Don't check modules that have less than 2 connected inputs
			int connectedInputCount = 0;
			for (Input i : it.getInputs()) {
				if (i.isConnected() || !(i instanceof Parameter)) {
					connectedInputCount++;
				}
				if (connectedInputCount > 1) {
					break;
				}
			}

			if (connectedInputCount > 1) {

				// Show a warning about all modules that were not ready
				if (!it.wasReady()) {
					StringBuilder notReady = new StringBuilder();

					for (Input input : it.getInputs()) {
						if (!input.wasReady()) {
							notReady.append(input.toString());
							notReady.append("\n");
							pushToUiChannel(new ModuleWarningMessage("Input was never ready: " + input.getEffectiveName(), input.getOwner().getHash()));
						}
					}
					log.debug("Module had non-ready inputs: " + notReady);
				}

			}

			// Clean up
			it.destroy();
		}
	}

	@Override
	public String getUiChannelName() {
		return "Notifications";
	}

	/**
	 * Sends a notification to this SignalPath's UI channel.
     */
	public void showNotification(@NotNull String notification) {
		pushToUiChannel(new NotificationMessage(notification));
	}

	@Override
	public void beforeSerialization() {
		super.beforeSerialization();
		for (AbstractSignalPathModule module : mods) {
			module.beforeSerialization();
		}
	}

	@Override
	public void afterSerialization() {
		super.afterSerialization();
		for (AbstractSignalPathModule module : mods) {
			module.afterSerialization();
		}
	}

	@Override
	public void afterDeserialization(SerializationService serializationService) {
		super.afterDeserialization(serializationService);
		for (AbstractSignalPathModule module : mods) {
			module.afterDeserialization(serializationService);
		}
	}

	/**
	 * Get streams on this canvas, NOT including subcanvases
	 * @return Streams on this canvas
	 */
	public Set<Stream> getStreams() {
		return mods.stream()
			.filter(mod -> mod instanceof AbstractStreamSourceModule)
			.map(mod -> ((AbstractStreamSourceModule)mod).getStream())
			.collect(toSet());
	}

	static class InputConnection {
		public Input input;
		public String connectedTo;

		public InputConnection(Input input, String connectedTo) {
			this.input = input;
			this.connectedTo = connectedTo;
		}
	}

	static class ModuleConfig implements Serializable {
		public AbstractSignalPathModule module;
		public Map config;

		public ModuleConfig(AbstractSignalPathModule module, Map config) {
			this.module = module;
			this.config = config;
		}
	}

	public List<Input> getExportedInputs() {
		return exportedInputs;
	}

	public List<Output> getExportedOutputs() {
		return exportedOutputs;
	}

	public Canvas getCanvas() {
		return canvas;
	}

	public void setCanvas(Canvas canvas) {
		this.canvas = canvas;
	}

	@Override
	public void setGlobals(Globals globals) {
		super.setGlobals(globals);

		for (AbstractSignalPathModule m : mods) {
			m.setGlobals(globals);
		}
	}

	@Override
	public AbstractSignalPathModule resolveRuntimeRequestRecipient(RuntimeRequest request, RuntimeRequest.PathReader path) {
		if (path.isEmpty()) {
			return super.resolveRuntimeRequestRecipient(request, path);
		}
		else {
			Integer moduleId = path.readModuleId();
			AbstractSignalPathModule module = modulesByHash.get(moduleId);
			if (module == null) {
				throw new IllegalArgumentException("Module not found: " + moduleId);
			} else {
				return module.resolveRuntimeRequestRecipient(request, path);
			}
		}
	}

	@Override
	protected RuntimeRequest.PathWriter getRuntimePath(RuntimeRequest.PathWriter writer) {
		return super.getRuntimePath(root ? writer.writeCanvasId(getCanvas().getId()) : writer);
	}

	@Override
	public SignalPath getRootSignalPath() {
		if (cachedRootSignalPath == null) {
			if (this.isRoot()) {
				cachedRootSignalPath = this;
			} else if (getParentSignalPath() != null) {
				cachedRootSignalPath = getParentSignalPath().getRootSignalPath();
			} else {
				throw new IllegalStateException("SignalPath is not root, but doesn't have a parent!");
			}
		}

		return cachedRootSignalPath;
	}
}
