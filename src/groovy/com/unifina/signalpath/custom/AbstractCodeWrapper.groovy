package com.unifina.signalpath.custom

import java.util.List;
import java.util.Map;

import com.unifina.signalpath.AbstractSignalPathModule
import com.unifina.utils.Globals
import com.unifina.utils.TimezoneConverter

abstract class AbstractCodeWrapper extends AbstractSignalPathModule {

	AbstractSignalPathModule instance = null
	String code = null
	
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
				String className = "CustomModule"+new Date().getTime()

				GroovyClassLoader gcl = new GroovyClassLoader()
				Class clazz = gcl.parseClass(makeImportString()+header.replace("[[CLASSNAME]]",className) + code + footer);
				instance = clazz.newInstance();
				instance.init()

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
