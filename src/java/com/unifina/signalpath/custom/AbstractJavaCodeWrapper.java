package com.unifina.signalpath.custom;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.tools.Diagnostic;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.unifina.security.UserJavaClassLoader;
import com.unifina.signalpath.AbstractSignalPathModule;
import com.unifina.signalpath.Input;
import com.unifina.signalpath.ModuleException;
import com.unifina.signalpath.ModuleExceptionMessage;
import com.unifina.signalpath.Output;

public abstract class AbstractJavaCodeWrapper extends AbstractSignalPathModule {

	AbstractSignalPathModule instance = null;
	String code = null;
	
	private static final Logger log = Logger.getLogger(AbstractJavaCodeWrapper.class);
	
	@Override
	public void init() {

	}

	@Override
	public void initialize() {
		if (instance!=null)
			instance.initialize();
	}
	
	@Override
	public void sendOutput() {
		instance.sendOutput();
	}

	@Override
	public void clearState() {
		if (instance!=null)
			instance.clear(); //clearState()
	}

	@Override
	public void onDay(Date day) {
		instance.onDay(day);
	}
	
	protected List<String> getImports() {
		return Arrays.asList(new String[] {
			"com.unifina.signalpath.custom.*",
			"com.unifina.signalpath.*",
			"com.unifina.orderbook.event.*",
			"com.unifina.order.*",
			"com.unifina.orderbook.*",
			"com.unifina.utils.*",
			"java.text.SimpleDateFormat",
			"com.unifina.event.*",
			"java.lang.*",
			"java.math.*",
			"java.util.*"
		});
	}
	
	protected abstract String getHeader();
	protected abstract String getDefaultCode();
	protected abstract String getFooter();
	
	@Override
	public Map<String,Object> getConfiguration() {
		Map<String,Object> config = super.getConfiguration();
		config.put("code", (code==null ? getDefaultCode() : code));
		return config;
	}

	private String makeImportString() {
		StringBuilder result = new StringBuilder();
		for (String i : getImports()) {
			result.append("import ");
			result.append(i);
			result.append(";\n");
		}
		return result.toString();
	}
	
	protected void onConfiguration(Map config) {
		super.onConfiguration(config);
		if (config.containsKey("code")) {
			
			code = config.get("code").toString();

			try {
				// Replace [CLASSNAME] with a generated one
				String className = "CustomModule"+System.currentTimeMillis()+Math.abs(code.hashCode());

				String fullCode = makeImportString()+getHeader().replace("[[CLASSNAME]]",className) + code + getFooter();
				
				UserJavaClassLoader ujcl = new UserJavaClassLoader(getClass().getClassLoader());
				boolean success = ujcl.parseClass(className, fullCode);
				if (!success) {
					StringBuilder sb = new StringBuilder();
					sb.append("Compilation errors:\n");
					
					List<ModuleExceptionMessage> msgs = new ArrayList<>();
					
					for (Diagnostic d : ujcl.getDiagnostics()) {
						long line = d.getLineNumber()-StringUtils.countMatches(makeImportString(), "\n")-StringUtils.countMatches(getHeader(), "\n");
						
						sb.append("Line ");
						sb.append(Long.toString(line));
						sb.append(": ");
						sb.append(d.getMessage(null));
						sb.append("\n");
						
						CompilationErrorMessage msg = new CompilationErrorMessage();
						msg.addError(line, d.getMessage(null));
						msgs.add(new ModuleExceptionMessage(hash,msg));
					}
					
					throw new ModuleException(sb.toString(),null,msgs);
				}
				Class<AbstractSignalPathModule> clazz = (Class<AbstractSignalPathModule>) ujcl.loadClass(className);
				
				// Register the created class so that it will be cleaned when Globals is destroyed
				// TODO: not needed for Java classes?
				globals.registerDynamicClass(clazz);
				
				// Create & init instance
				instance = clazz.newInstance();
				instance.init();

				for (Input it : instance.getInputs())
					addInput(it);

				for (Output it : instance.getOutputs())
					addOutput(it);

				// Inject stuff into the module
				instance.globals = globals;
				instance.setHash(hash);
				instance.setParentSignalPath(parentSignalPath);
			}
			catch (Exception e) {
				// TODO: How to allow saving of invalid code? If it doesn't get compiled, inputs etc. won't be found
//				if (!globals.target.save) {
					throw new RuntimeException(e);
//				}
			}
		}
	}


	
}
