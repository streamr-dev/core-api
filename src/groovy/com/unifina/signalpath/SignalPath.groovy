package com.unifina.signalpath

import grails.converters.JSON

import org.apache.log4j.Logger

import com.unifina.ModuleService
import com.unifina.datasource.ITimeListener
import com.unifina.signalpath.charts.Chart
import com.unifina.utils.Globals

class SignalPath extends AbstractSignalPathModule {
	
	private static final Logger log = Logger.getLogger(SignalPath)
	
	SignalPathParameter sp = new SignalPathParameter(this,"signalPath") {
		@Override
		public Map<String,Object> getConfiguration() {
			Map<String,Object> config = super.getConfiguration()
			config.put("updateOnChange", true)
			return config
		}
	}
	
//	List orderBooks = []
	List modulesJSON = []
	List<AbstractSignalPathModule> mods = []
	
	List recorders = []
	List timeListeners = []
	
	List<Input> exportedInputs = []
	List<Output> exportedOutputs = []
	
	public SignalPathReturnChannel returnChannel
	
	Map representation = null
	Map<Integer,AbstractSignalPathModule> modulesByHash = new HashMap<>()
	
	ModuleService moduleService = new ModuleService()
	
	private boolean isRoot = false
	
	public SignalPath() {
		super()
		initPriority = 10
		canRefresh = true
	}
	
	public SignalPath(Map iData, boolean isRoot, Globals globals) {
		super()
		this.isRoot = isRoot
		this.globals = globals
		initPriority = 10
		canRefresh = true
		
		// Backwards compatibility, TODO: remove this constructor eventually
		initFromRepresentation(iData)
	}
	
	private void initFromRepresentation(Map iData) {
		representation = iData
		name = iData.name ?: name
		
		Map inputs = [:]
		Map outputs = [:]
		
		// TODO: remove backwards compatibility
		if (iData.orderBooks) {
			iData.orderBooks.each {json->
				// For backwards compatibility
				if (json.orderBookId==null) {
					json.orderBookId = json.id
					json.id = 68
				}

				iData.modules.add(json)

			}
		}

		
		// TODO: remove backwards compatibility
		if (iData.charts) {
			iData.charts.each {json->
				if (json.id==null || json.id==0)
					json.id = 67

				iData.modules.add(json)
			}
		}
		
		// TODO: remove backwards compatibility
		iData.modules.each {json->
			if (json.id==null || json.id==0)
				json.id = 67
			if (json.id > 1000 || json.id==-1) {
				json.orderBookId = json.id
				json.id = 68
			}
		}

		iData.modules.each {module->
			
			Module m = Module.get(module.id)
			AbstractSignalPathModule mod = moduleService.getModuleInstance(m,module,this,globals)
//			mod.parentSignalPath = this
			
			if (mod instanceof ITimeListener)
				timeListeners.add(mod)

//			mod.configuration = module

			
			// Get parameter inputs and outputs if connected, or create constants if not connected
			module.params.each {param->
				Input i = mod.getInput(param.name)
//				i.configuration = param
				
				if (i==null)
					println "No such parameter: "+param.name
				else {
					if (param.connected)
						inputs.put(param.endpointId, [source:param.sourceId, input:i])
					else {
						// Param value has already been set in configuration. Should something more be done?
					}

					if (param.export)
						exportedInputs.add(i)
				}
			}
			
			modulesJSON.add([json:module,module:mod]);
			mods.add(mod)
		}
		
		// Collect inputs and outputs with endpoints from modules
		modulesJSON.each {data->
			def json = data.json
			def mod = data.module

			json.inputs.each {input->
				Input i = mod.getInput(input.name)
				if (i==null)
					println "No such input: "+input.name
//					throw new RuntimeException("No such input: "+input.name)
				else {
//					i.configuration = input

					if (input.connected)
						inputs.put(input.endpointId, [source:input.sourceId, input:i])

					if (input.export)
						exportedInputs.add(i)
				}
			}
			
			json.outputs.each {output->
				Output o = mod.getOutput(output.name)
				if (o==null)
					println "No such output: "+output.name
//					throw new RuntimeException("No such output: "+output.name)
				else {
//					o.configuration = output

					if (output.connected)
						outputs.put(output.id, o)
					if (output.export)
						exportedOutputs.add(o)
				}
			}
			
		}
		
		// Wire every connected input to its source output
		inputs.each {id,input->
			Input i = input.input
			Output o = outputs.get(input.source)
			if (o!=null)
				o.connect(i)
			else println "Input $i.name could not be connected, because source was not found. SP: ${i.owner.parentSignalPath}, TOP: ${i.owner.topParentSignalPath}"
		}
		
		/**
		 * Exported inputs may be exported with another name if the io names
		 * in different modules clash
		 */
		if (!isRoot) {
			exportedInputs.each {
				// Don't retain the saved json configuration
				it.resetConfiguration()
				
				if (getInput(it.name)==null) {
					addInput(it)
				}
				// Resolve name conflict
				else {
					int counter = 2

					while(getInput(it.name+counter)!=null)
						counter++

					 it.name = it.name+counter
					 addInput(it)
//					addInput(it,it.name+counter)
				}
			}
			exportedOutputs.each {
				// Don't retain the saved json configuration
				it.resetConfiguration()
				
				if (getOutput(it.name)==null) {
					addOutput(it)
				}
				// Resolve name conflict
				else {
					int counter = 2

					while(getOutput(it.name+counter)!=null)
						counter++

					it.name = it.name+counter
					addOutput(it)
//					addOutput(it,it.name+counter)
				} 
			}
		}
		
		this.clearState = isDistributable()
	}
	
	@Deprecated
	public List getTimelisteners() {
		return modulesJSON.collect {return it.module}.findAll {it instanceof ITimeListener}
//		return timeListeners
	}
	
	@Deprecated
	public List getRecorders() {
		return modulesJSON.findAll {it.module instanceof Chart}
//		return recorders
	}
	
	public List<AbstractSignalPathModule> getModules() {
		return mods;
	}
	
	public AbstractSignalPathModule getModule(int hash) {
		return modulesByHash.get(hash)
	}
	
	/**
	 * A SignalPath is distributable if is independent over days, ie.
	 * if no module or connection has overnight state.
	 * @return
	 */
	public boolean isDistributable() {
		boolean overnightState = false
		modulesJSON.each {module->
			AbstractSignalPathModule m = module.module
			if (!m.clearState)
				overnightState = true
//			Input[] inputs = m.getInputs()
//			inputs.each {input->
//				if (!input.clearState && input.isConnected())
//					overnightState = true
//			}
		}
		return !overnightState
	}
	
	public boolean hasExports() {
		return exportedInputs.size() + exportedOutputs.size() > 0
	}
	
//	@Deprecated
//	public Map getExportProperties() {
//		Map result = [
//			exportedStringParameters: 0,
//			exportedBooleanParameters: 0,
//			exportedIntegerParameters: 0,
//			exportedDoubleParameters: 0,
//			exportedSignalPathParameters: 0,
//			exportedTimeSeriesInputs: 0,
//			exportedTradesInputs: 0,
//			exportedOrderbookInputs: 0,
//			exportedTimeSeriesOutputs: 0,
//			exportedTradesOutputs: 0,
//			exportedOrderbookOutputs: 0
//		]
//		
//		exportedInputs.each {
//			if (it instanceof StringParameter)
//				result.exportedStringParameters++
//			else if (it instanceof BooleanParameter)
//				result.exportedBooleanParameters++
//			else if (it instanceof IntegerParameter)
//				result.exportedIntegerParameters++
//			else if (it instanceof DoubleParameter)
//				result.exportedDoubleParameters++
//			// TODO: this is not general
//			else if (it instanceof SignalPathParameter)
//				result.exportedSignalPathParameters++
//			else if (it instanceof TimeSeriesInput)
//				result.exportedTimeSeriesInputs++
//			// TODO: this is not general
//			else if (it instanceof TradesInput)
//				result.exportedTradesInputs++
//			// TODO: not general
//			else if (it instanceof OrderbookInput)
//				result.exportedOrderbookInputs++
////			else throw new RuntimeException("Unknown type of exported input: "+it)
//		}
//		
//		exportedOutputs.each {
//			if (it instanceof TimeSeriesOutput)
//				result.exportedTimeSeriesOutputs++
//				// TODO: not general
//			else if (it instanceof TradesOutput)
//				result.exportedTradesOutputs++
//				// TODO: not general
//			else if (it instanceof OrderbookOutput)
//				result.exportedOrderbookOutputs++
////			else throw new RuntimeException("Unknown type of exported output: "+it)
//		}
//		
//		return result
//	}
	
	@Override
	public void onConfiguration(Map config) {
		super.onConfiguration(config)
//		if (config.containsKey("representation"))
//			initFromRepresentation(config.representation)
		if (sp.value!=null) {
//			def spData = config.params.find { it.name=="signalPath" }
//			sp.configuration = spData
			
			initFromRepresentation(JSON.parse(sp.value.json).signalPathData)
		}
	}
	
	@Override
	public void connectionsReady() {
		super.connectionsReady()

		modulesJSON.each {
			modulesByHash.put(it.module.hash,it.module)
		}
		List<AbstractSignalPathModule> sortedModules = modulesJSON.collect{it.module}.sort{it.initPriority}
		for (int i=0;i<sortedModules.size();i++) {
			sortedModules.get(i).connectionsReady()
		}
//		modulesJSON.sort{it.module.initPriority}.each {it.module.connectionsReady()}
		
		globals.dataSource?.connectSignalPath(this)
	}
	
	@Override
	public void sendOutput() {
		
	}
	
	@Override
	public void clearState() {
		
	}
	
	@Override
	public void init() {
		addInput(sp)
	}
	
	@Override
	public void destroy() {
		super.destroy();
		
		mods.each {AbstractSignalPathModule it->
			// Don't check modules that have less than 2 connected inputs
			
			if (it.getInputs().findAll {input-> return (input.isConnected() || !(input instanceof Parameter))}.size() > 1) {

				// Show a warning about all modules that were not ready
				if (!it.wasReady()) {
					def notReady = ""
					it.getInputs().each {input->
						if (!input.wasReady()) {
							notReady += "$input\n"
							if (parentSignalPath?.returnChannel) {
								parentSignalPath.returnChannel.sendPayload(it.getHash(), [type:"warning",msg:"Input was never ready: $input.name"])
							}
						}
					}
					log.debug("Module had non-ready inputs: $notReady")
				}
				
			}
			// Clean up
			it.destroy() 
		}
	}
}
