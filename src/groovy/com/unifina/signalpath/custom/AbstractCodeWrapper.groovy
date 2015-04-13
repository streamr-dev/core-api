package com.unifina.signalpath.custom

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger
import org.codehaus.groovy.control.ErrorCollector
import org.codehaus.groovy.control.MultipleCompilationErrorsException
import org.codehaus.groovy.syntax.SyntaxException

import com.unifina.security.UserClassLoader
import com.unifina.service.ModuleService
import com.unifina.signalpath.AbstractSignalPathModule
import com.unifina.signalpath.ModuleException;
import com.unifina.signalpath.ModuleExceptionMessage;
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
		"com.unifina.utils.*",
		"java.text.SimpleDateFormat",
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
			catch (MultipleCompilationErrorsException e) {
				StringBuilder sb = new StringBuilder();
				sb.append("Compilation errors:\n");
				
				List<ModuleExceptionMessage> msgs = new ArrayList<>();
				ErrorCollector ec = e.getErrorCollector()
				for (int i=0; i<ec.getErrorCount();i++) {
					if (ec.getSyntaxError(i)!=null) {
						SyntaxException se = ec.getSyntaxError(i)
						
						long line = se.getLine()-StringUtils.countMatches(makeImportString(), "\n")-StringUtils.countMatches(getHeader(), "\n");
						
						sb.append("Line ");
						sb.append(Long.toString(line));
						sb.append(": ");
						sb.append(se.getMessage());
						sb.append("\n");
						
						CompilationErrorMessage msg = new CompilationErrorMessage();
						msg.addError(line, se.getMessage());
						msgs.add(new ModuleExceptionMessage(hash,msg));
					}
				}
				throw new ModuleException(sb.toString(),null,msgs);
			}
			catch (Exception e) {
				// TODO: How to allow saving of invalid code? If it doesn't get compiled, inputs etc. won't be found
				throw e
			}
		}
	}
	//TODO: Does the compiled class remain in the JVM, or can it be purged somehow? "ClassUnloader?"

}
