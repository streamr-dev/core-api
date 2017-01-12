package com.unifina.signalpath;

import com.unifina.data.FeedEvent;
import com.unifina.domain.signalpath.Canvas;
import com.unifina.domain.signalpath.Module;
import com.unifina.push.PushChannel;
import com.unifina.serialization.SerializationRequest;
import com.unifina.service.CanvasService;
import com.unifina.service.ModuleService;
import com.unifina.service.SerializationService;
import com.unifina.utils.Globals;
import grails.converters.JSON;
import org.apache.log4j.Logger;
import org.codehaus.groovy.grails.web.json.JSONObject;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.*;

public class SignalPath extends ModuleWithUI {

	private static final Logger log = Logger.getLogger(SignalPath.class);
	private static Comparator<AbstractSignalPathModule> initPriorityComparator = new Comparator<AbstractSignalPathModule>() {
		@Override
		public int compare(AbstractSignalPathModule o1, AbstractSignalPathModule o2) {
			return Integer.compare(o1.getInitPriority(), o2.getInitPriority());
		}
	};

	SignalPathParameter sp;

	List<ModuleConfig> moduleConfigs = new ArrayList<>();
	List<AbstractSignalPathModule> mods = new ArrayList<>();

	List<Input> exportedInputs = new ArrayList<Input>();
	List<Output> exportedOutputs = new ArrayList<Output>();

	Canvas canvas = null;
	Map representation = null;
	Map<Integer, AbstractSignalPathModule> modulesByHash = new HashMap<>();

	private boolean root = false;

	public SignalPath() {
		super();
		initPriority = 10;
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
		initPriority = 10;
		canRefresh = true;

		// Backwards compatibility, TODO: remove this constructor eventually
		initFromRepresentation(iData);
	}

	// TODO: remove backwards compatibility eventually
	@Override
	public Input getInput(String name) {
		if (name.equals("signalPath") || name.equals("streamlet")) { name = "canvas"; }
		return super.getInput(name);
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	private void initFromRepresentation(Map iData) {
		representation = iData;
		if (iData.get("name") != null) {
			setName(iData.get("name").toString());
		}

		List<InputConnection> inputs = new ArrayList<>();
		Map<String, Output> outputs = new HashMap<>();

		List<Map> modulesJSON = (List<Map>) iData.get("modules");
		if (modulesJSON == null) {
			modulesJSON = new ArrayList<>(0);
		}

		ModuleService moduleService = getGlobals().getBean(ModuleService.class);

		HashMap<Long, Module> moduleDomainById = new HashMap<>();
		for (Module m : moduleService.getModuleDomainObjects(modulesJSON)) {
			moduleDomainById.put(m.getId(), m);
		}

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
				log.warn("Input " + ic.input.getName() + " could not be connected, because source was not found. SP: " + ic.input.getOwner().getParentSignalPath() + ", TOP: " + ic.input.getOwner().getTopParentSignalPath());
			}
		}

		// Exported inputs may be exported with another name
		// If the io names in different modules clash, add a running number in the end to make the name unique
		if (!root) {
			for (Input it : exportedInputs) {
				// Ensure variadic endpoints are imported as normal endpoints
				it.setJsClass(null);
				// Prevent exported Endpoints from being exported on wrapping module
				it.setExported(false);
				// Id needs to be regenerated to avoid clashes with other instances of the same canvas-as-a-module
				it.regenerateId();
				if (getInput(it.name) == null) {
					addInput(it);
				}
				else {
					int counter = 2;
					while (getInput(it.name + counter) != null) {
						counter++;
					}
					it.name = it.name + counter;
					addInput(it);
				}
			}
			for (Output it : exportedOutputs) {
				// Ensure variadic endpoints are imported as normal endpoints
				it.setJsClass(null);
				// Prevent exported Endpoints from being exported on wrapping module
				it.setExported(false);
				// Id needs to be regenerated to avoid clashes with other instances of the same canvas-as-a-module
				it.regenerateId();
				if (getOutput(it.name) == null) {
					addOutput(it);
				}
				else {
					int counter = 2;
					while (getOutput(it.name + counter) != null) {
						counter++;
					}
					it.name = it.name + counter;
					addOutput(it);
				}
			}
		}
	}

	public List<AbstractSignalPathModule> getModules() {
		return mods;
	}

	public AbstractSignalPathModule getModule(int hash) {
		return modulesByHash.get(hash);
	}

	public boolean hasExports() {
		return exportedInputs.size() + exportedOutputs.size() > 0;
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
		if (!root && sp.getValue() != null) {
			/**
			 * Reset uiChannels if this is a subcanvas (not the root canvas). Otherwise
			 * there will be problems if many instances of the same canvas are used
			 * as subcanvases, as all the instances would produce to same uiChannels.
			 */
			Map json = (JSONObject) JSON.parse(sp.getValue().getJson());
			getGlobals().getBean(CanvasService.class).resetUiChannels(json);
			initFromRepresentation(json);
		} else {
			initFromRepresentation(config);
		}
	}

	@Override
	public void connectionsReady() {
		super.connectionsReady();

		List<AbstractSignalPathModule> sortedModules = new ArrayList<>();
		for (ModuleConfig mc : moduleConfigs) {
			modulesByHash.put(mc.module.hash, mc.module);
			sortedModules.add(mc.module);
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

	}

	@Override
	public void init() {
		if (!root) {
			sp = new SignalPathParameter(this, "canvas");
			sp.setUpdateOnChange(true);
			addInput(sp);
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

							if (getGlobals().getUiChannel() != null) {
								getGlobals().getUiChannel().push(new ModuleWarningMessage("Input was never ready: " + input.getEffectiveName(), input.getOwner().getHash()), uiChannelId);
							}
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
		PushChannel pushChannel = getGlobals().getUiChannel();
		if (pushChannel != null) {
			pushChannel.push(new NotificationMessage(notification), getUiChannelId());
		}
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

	class InputConnection {
		public Input input;
		public String connectedTo;

		public InputConnection(Input input, String connectedTo) {
			this.input = input;
			this.connectedTo = connectedTo;
		}
	}

	class ModuleConfig implements Serializable {
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

	public void setExportedInputs(List<Input> exportedInputs) {
		this.exportedInputs = exportedInputs;
	}

	public List<Output> getExportedOutputs() {
		return exportedOutputs;
	}

	public void setExportedOutputs(List<Output> exportedOutputs) {
		this.exportedOutputs = exportedOutputs;
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
			AbstractSignalPathModule module = getModule(moduleId);
			if (module == null) {
				throw new IllegalArgumentException("Module not found: "+moduleId);
			}
			else {
				return module.resolveRuntimeRequestRecipient(request, path);
			}
		}
	}
}
