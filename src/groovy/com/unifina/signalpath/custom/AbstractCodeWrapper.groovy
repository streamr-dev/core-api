package com.unifina.signalpath.custom

import org.apache.log4j.Logger

import com.unifina.security.UserClassLoader
import com.unifina.service.ModuleService
import com.unifina.signalpath.AbstractSignalPathModule
import com.unifina.utils.Globals

abstract class AbstractCodeWrapper extends AbstractSignalPathModule {

	AbstractSignalPathModule instance = null
	String code = null
	
	private static final Logger log = Logger.getLogger(AbstractCodeWrapper.class)
	
	@Override
	public void init() {

	}

	@Override
	public void initialize() {
		if (instance!=null)
			instance.initialize()
	}
	
	@Override
	public void sendOutput() {
		instance.sendOutput()
	}

	@Override
	public void clearState() {
		if (instance!=null)
			instance.clear() //clearState()
	}

	@Override
	public void onDay(Date day) {
		instance.onDay(day);
	}
	
	List<String> getImports() {return [
		"com.unifina.signalpath.custom.*",
		"com.unifina.signalpath.*",
		"com.unifina.orderbook.event.*",
		"com.unifina.order.*",
		"com.unifina.orderbook.*",
		"com.unifina.utils.*",
		"java.text.SimpleDateFormat",
		"com.unifina.event.*"
	]}
	
	abstract String getHeader()
	abstract String getDefaultCode()
	abstract String getFooter()
	
	@Override
	public Map<String,Object> getConfiguration() {
		Map<String,Object> config = super.getConfiguration()
		config.put("code", code ?: getDefaultCode())
		return config
	}

	private String makeImportString() {
		String result = ""
		getImports().each {
			result += "import " + it + "; "
		}
		return result
	}
	
	void onConfiguration(Map config) {
		super.onConfiguration(config)
		if (config.containsKey("code")) {
			
			code = config.get("code")

			try {
				// Replace [CLASSNAME] with a generated one
				String className = "CustomModule"+UUID.randomUUID().toString().replace("-","_")//(System.currentTimeMillis()+Math.abs(code.hashCode()))

				// Avoid using the Thread context classloader
//				ClassLoader parentClassLoader = this.class.getClassLoader()
//				GroovyClassLoader gcl = new GroovyClassLoader(parentClassLoader)
				
				// Load the class using the classloader of the Globals class so that the classes loaded
				// for this SignalPath run can be unloaded when the run finishes.
				String code = makeImportString()+header.replace("[[CLASSNAME]]",className) + code + footer;
				Class clazz = new UserClassLoader(ModuleService.class.getClassLoader()).parseClass(code,className+".groovy")
				
				// Register the created class so that it will be cleaned when Globals is destroyed
				globals.registerDynamicClass(clazz);
				
				// Create & init instance
				instance = clazz.newInstance();
				instance.init()

//				ClassLoader parentCl = MetaClass.class.getClassLoader()
//				log.warn("MetaClass classloader: $parentCl, parallel: "+(parentCl.getClassLoadingLock(className+"MetaClass") instanceof ClassLoader ? "false" : "true"))
//				while (parentCl.getParent()!=null) {
//					parentCl = parentCl.getParent()
//					log.warn("Parent classloader: $parentCl, parallelLockMap: "+parentCl.parallelLockMap!=null ? "exists" : "does not exist")
//				}
				
				instance.inputs.each {
					addInput(it)
				}
				instance.outputs.each {
					addOutput(it)
				}

				// Inject stuff into the module
				instance.globals = globals
//				instance.tcConverter = new TimezoneConverter(globals.userTimeZone)
				instance.hash = hash
				instance.parentSignalPath = parentSignalPath
			}
			catch (Exception e) {
				// TODO: How to allow saving of invalid code? If it doesn't get compiled, inputs etc. won't be found
//				if (!globals.target.save) {
					throw e
//				}
			}
		}
	}
	//TODO: Does the compiled class remain in the JVM, or can it be purged somehow? "ClassUnloader?"

}
