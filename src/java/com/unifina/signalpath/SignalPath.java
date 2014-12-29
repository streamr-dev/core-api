package com.unifina.signalpath;

import grails.converters.JSON;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.codehaus.groovy.grails.web.json.JSONObject;

import com.unifina.domain.signalpath.Module;
import com.unifina.service.ModuleService;
import com.unifina.utils.Globals;

public class SignalPath extends ModuleWithUI {
	
	private static final Logger log = Logger.getLogger(SignalPath.class);
	private static Comparator<AbstractSignalPathModule> initPriorityComparator = new Comparator<AbstractSignalPathModule>() {
		@Override
		public int compare(AbstractSignalPathModule o1,
				AbstractSignalPathModule o2) {
			return Integer.compare(o1.getInitPriority(), o2.getInitPriority());
		}
	};
	
	SignalPathParameter sp;
	
	List<ModuleConfig> moduleConfigs = new ArrayList<>();
	List<AbstractSignalPathModule> mods = new ArrayList<>();
	
	List<Input> exportedInputs = new ArrayList<Input>();
	List<Output> exportedOutputs = new ArrayList<Output>();
	
	Map representation = null;
	Map<Integer,AbstractSignalPathModule> modulesByHash = new HashMap<>();
	
	ModuleService moduleService = new ModuleService();
	
	private boolean isRoot = false;
	
	public SignalPath() {
		super();
		initPriority = 10;
		canRefresh = true;
	}
	
	public SignalPath(Map iData, boolean isRoot, Globals globals) {
		super();
		this.isRoot = isRoot;
		this.globals = globals;
		initPriority = 10;
		canRefresh = true;
		
		// Backwards compatibility, TODO: remove this constructor eventually
		initFromRepresentation(iData);
	}
	
	// TODO: remove backwards compatibility eventually
	@Override
	public Input getInput(String name) {
		if (name.equals("signalPath") || name.equals("streamlet"))
			name = "canvas";
		return super.getInput(name);
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void initFromRepresentation(Map iData) {
		representation = iData;
		name = (iData.containsKey("name") && iData.get("name")!=null ? iData.get("name").toString() : name);
		
		List<InputConnection> inputs = new ArrayList<>();
		Map<String,Output> outputs = new HashMap<>();
		
		List<Map> modulesJSON = (List<Map>) iData.get("modules");
		
		HashMap<Long,Module> moduleDomainById = new HashMap<>();
		for (Module m : moduleService.getModuleDomainObjects(modulesJSON))
			moduleDomainById.put(m.getId(), m);
		
		for (Map moduleConfig : modulesJSON) {
			
			Module moduleDomain = moduleDomainById.get(((Number) moduleConfig.get("id")).longValue());
			
			AbstractSignalPathModule moduleImpl = moduleService.getModuleInstance(moduleDomain,moduleConfig,this,globals);
			
			// Get parameter inputs and outputs if connected, or create constants if not connected
			for (Map paramConfig : (List<Map>) moduleConfig.get("params")) {
				Input i = moduleImpl.getInput(paramConfig.get("name").toString());
				
				if (i==null)
					log.warn("No such parameter: "+paramConfig.get("name"));
				else {
					if (paramConfig.containsKey("connected") && (boolean) paramConfig.get("connected")) {
						inputs.add(new InputConnection(i,paramConfig.get("sourceId").toString()));
					}
					else {
						// Param value has already been set in configuration. Should something more be done?
					}

					if (paramConfig.containsKey("export") && (boolean)paramConfig.get("export"))
						exportedInputs.add(i);
				}
			}
			
			moduleConfigs.add(new ModuleConfig(moduleImpl,moduleConfig));
			mods.add(moduleImpl);
		}
		
		// Collect inputs and outputs with endpoints from modules
		for (ModuleConfig mc : moduleConfigs) {
			AbstractSignalPathModule mod = mc.module;

			for (Map inputConfig : (List<Map>) mc.config.get("inputs")) {
				Input i = mod.getInput(inputConfig.get("name").toString());
				if (i==null)
					log.warn("No such input: "+inputConfig.get("name"));
				else {

					if (inputConfig.containsKey("connected") && (boolean)inputConfig.get("connected"))
						inputs.add(new InputConnection(i,inputConfig.get("sourceId").toString()));

					if (inputConfig.containsKey("export") && (boolean)inputConfig.get("export"))
						exportedInputs.add(i);
				}
			}
			
			for (Map outputConfig : (List<Map>) mc.config.get("outputs")) {

				Output o = mod.getOutput(outputConfig.get("name").toString());
				if (o==null)
					log.warn("No such output: "+outputConfig.get("name"));
				else {

					if (outputConfig.containsKey("connected") && (boolean)outputConfig.get("connected") && outputConfig.get("id")!=null)
						outputs.put(outputConfig.get("id").toString(), o);
					
					if (outputConfig.containsKey("export") && (boolean)outputConfig.get("export"))
						exportedOutputs.add(o);
				}
			}
			
		}
		
		// Wire every connected input to its source output
		for (InputConnection ic : inputs) {
			Output o = outputs.get(ic.connectedTo);
			if (o!=null)
				o.connect(ic.input);
			else log.warn("Input "+ic.input.getName()+" could not be connected, because source was not found. SP: "+ic.input.getOwner().getParentSignalPath()+", TOP: "+ic.input.getOwner().getTopParentSignalPath());
		}
		
		/**
		 * Exported inputs may be exported with another name if the io names
		 * in different modules clash
		 */
		if (!isRoot) {
			for (Input it : exportedInputs) {
				// Don't retain the saved json configuration
				it.resetConfiguration();
				
				if (getInput(it.name)==null) {
					addInput(it);
				}
				// Resolve name conflict
				else {
					int counter = 2;

					while(getInput(it.name+counter)!=null)
						counter++;

					 it.name = it.name+counter;
					 addInput(it);
				}
			}
			for (Output it : exportedOutputs) {
				// Don't retain the saved json configuration
				it.resetConfiguration();
				
				if (getOutput(it.name)==null) {
					addOutput(it);
				}
				// Resolve name conflict
				else {
					int counter = 2;

					while(getOutput(it.name+counter)!=null)
						counter++;

					it.name = it.name+counter;
					addOutput(it);
				} 
			}
		}
		
		this.clearState = isDistributable();
	}
	
	public List<AbstractSignalPathModule> getModules() {
		return mods;
	}
	
	public AbstractSignalPathModule getModule(int hash) {
		return modulesByHash.get(hash);
	}
	
	/**
	 * A SignalPath is distributable if it is independent over days, ie.
	 * if no module or connection has overnight state.
	 * @return
	 */
	@Deprecated
	public boolean isDistributable() {
		boolean overnightState = false;
		for (ModuleConfig mc : moduleConfigs) {
			AbstractSignalPathModule m = mc.module;
			if (!m.clearState) {
				overnightState = true;
				break;
			}
		}
		return !overnightState;
	}
	
	public boolean hasExports() {
		return exportedInputs.size() + exportedOutputs.size() > 0;
	}
	
	@Override
	public void onConfiguration(Map config) {
		super.onConfiguration(config);
		if (sp!=null && sp.value!=null) {
			initFromRepresentation(((JSONObject) JSON.parse(sp.value.getJson())).getJSONObject("signalPathData"));
		}
	}
	
	@Override
	public void connectionsReady() {
		super.connectionsReady();

		List<AbstractSignalPathModule> sortedModules = new ArrayList<>();
		for (ModuleConfig mc : moduleConfigs) {
			modulesByHash.put(mc.module.hash,mc.module);
			sortedModules.add(mc.module);
		}
		
		// Call connectionsReady in modules in ascending init priority order
		Collections.sort(sortedModules, initPriorityComparator);
		
		for (int i=0;i<sortedModules.size();i++) {
			sortedModules.get(i).connectionsReady();
		}
		
		if (globals.getDataSource()!=null)
			globals.getDataSource().connectSignalPath(this);
	}
	
	@Override
	public void sendOutput() {
		
	}
	
	@Override
	public void clearState() {
		
	}
	
	@Override
	public void init() {
		sp = new SignalPathParameter(this,"canvas");
		sp.setUpdateOnChange(true);
		addInput(sp);
	}
	
	@Override
	public void initialize() {
		super.initialize();
		// Embedded SignalPaths inherit the uiChannelId of their parent
		if (parentSignalPath!=null && parentSignalPath.getUiChannelId()!=null)
			uiChannelId = parentSignalPath.getUiChannelId();
	}
	
	@Override
	public void destroy() {
		super.destroy();
		
		for (AbstractSignalPathModule it : mods) {
			// Don't check modules that have less than 2 connected inputs
			int connectedInputCount = 0;
			for (Input i : it.getInputs()) {
				if (i.isConnected() || !(i instanceof Parameter))
					connectedInputCount++;
				if (connectedInputCount>1)
					break;
			}
			
			if (connectedInputCount > 1) {

				// Show a warning about all modules that were not ready
				if (!it.wasReady()) {
					StringBuilder notReady = new StringBuilder();
					
					for (Input input : it.getInputs()) {
						if (!input.wasReady()) {
							notReady.append(input.toString());
							notReady.append("\n");

							if (globals.getUiChannel()!=null) {
								globals.getUiChannel().push(new ModuleWarningMessage("Input was never ready: "+input.name, hash), uiChannelId);
							}
						}
					}
					log.debug("Module had non-ready inputs: "+notReady);
				}
				
			}
			
			// Clean up
			it.destroy(); 
		}
	}
	
	class InputConnection {
		public Input input;
		public String connectedTo;
		
		public InputConnection (Input input, String connectedTo) {
			this.input = input;
			this.connectedTo = connectedTo;
		}
	}
	
	class ModuleConfig {
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

}
