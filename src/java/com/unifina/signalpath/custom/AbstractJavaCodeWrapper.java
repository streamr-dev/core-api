package com.unifina.signalpath.custom;

import java.util.*;

import javax.tools.Diagnostic;

import com.unifina.serialization.HiddenFieldDetector;
import com.unifina.service.SerializationService;
import com.unifina.signalpath.*;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.unifina.security.UserJavaClassLoader;

public abstract class AbstractJavaCodeWrapper extends ModuleWithUI {

	transient AbstractCustomModule instance = null;
	String serializedInstance = null;
	String code = null;
	String className = null;
	String fullCode = null;
	StoredEndpointFields storedEndpointFields = null;
	transient UserJavaClassLoader classLoader = null;

	private static final Logger log = Logger.getLogger(AbstractJavaCodeWrapper.class);

	@Override
	public void initialize() {
		if (instance != null) {
			instance.initialize();
		}
	}

	@Override
	public void trySendOutput() {
		instance.trySendOutput();
	}

	@Override
	public void connectionsReady() {
		super.connectionsReady();
		instance.connectionsReady();
	}

	@Override
	public void setClearState(boolean clearState) {
		super.setClearState(clearState);
		instance.setClearState(clearState);
	}

	@Override
	public void sendOutput() {
		instance.sendOutput();
	}

	@Override
	public void clearState() {
		if (instance != null) {
			instance.clear();
		}
	}

	@Override
	public void onDay(Date day) {
		instance.onDay(day);
	}
	
	protected List<String> getImports() {
		return Arrays.asList(new String[] {
			"com.unifina.signalpath.custom.*",
			"com.unifina.signalpath.*",
			"com.unifina.utils.*",
			"java.text.SimpleDateFormat",
			"java.lang.*",
			"java.math.*",
			"java.util.*"
		});
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
	
	protected abstract String getHeader();
	protected abstract String getDefaultCode();
	protected abstract String getFooter();
	
	@Override
	public Map<String,Object> getConfiguration() {
		Map<String,Object> config = super.getConfiguration();
		config.put("code", (code==null ? getDefaultCode() : code));
		return config;
	}

	protected void onConfiguration(Map config) {
		super.onConfiguration(config);
		if (config.containsKey("code")) {

			code = config.get("code").toString();

			try {
				className = generateClassName();
				fullCode = combineCode(className);
				Class<AbstractCustomModule> clazz = compileAndRegister(className, fullCode);
				instance = setUpCustomModule(clazz, config);
			}
			catch (Exception e) {
				if (e instanceof ModuleException) {
					throw (ModuleException) e;
				}
				// TODO: How to allow saving of invalid code? If it doesn't get compiled, inputs etc. won't be found
//				if (!globals.target.save) {
				throw new RuntimeException(e);
//				}
			}
		}
	}

	private String generateClassName() {
		return "CustomModule" + System.currentTimeMillis() + Math.abs(code.hashCode());
	}

	private String combineCode(String className) {
		// Replace [CLASSNAME] with a generated one
		return makeImportString() + getHeader().replace("[[CLASSNAME]]", className) + code + getFooter();
	}

	private Class<AbstractCustomModule> compileAndRegister(String className, String fullCode) throws ClassNotFoundException {
		classLoader = new UserJavaClassLoader(getClass().getClassLoader());
		boolean success = classLoader.parseClass(className, fullCode);

		HiddenFieldDetector detector = null;
		if (success) {
			detector = new HiddenFieldDetector(classLoader.loadClass(className));
			success = !detector.anyHiddenFields();
		}

		if (!success) {
			StringBuilder sb = new StringBuilder();
			sb.append("Compilation errors:\n");

			List<ModuleExceptionMessage> msgs = new ArrayList<>();

			for (Diagnostic d : classLoader.getDiagnostics()) {
				long line = d.getLineNumber()- StringUtils.countMatches(makeImportString(), "\n")-StringUtils.countMatches(getHeader(), "\n");

				sb.append("Line ");
				sb.append(Long.toString(line));
				sb.append(": ");
				sb.append(d.getMessage(null));
				sb.append("\n");

				CompilationErrorMessage msg = new CompilationErrorMessage();
				msg.addError(line, d.getMessage(null));
				msgs.add(new ModuleExceptionMessage(hash,msg));
			}

			if (detector != null) {
				for (String fieldName : detector.hiddenFields().keySet()) {

					String message = "Hiding of field '" + fieldName + "' not allowed. Declarations " +
							"in inheritance chain by classes: " + detector.hiddenFields().get(fieldName);

					sb.append("Line ");
					sb.append(0);
					sb.append(": ");
					sb.append(message);
					sb.append("\n");

					CompilationErrorMessage msg = new CompilationErrorMessage();
					msg.addError(0, message);
					msgs.add(new ModuleExceptionMessage(hash, msg));
				}
			}

			throw new ModuleException(sb.toString(),null,msgs);
		}

		// Register the created class so that it will be cleaned when Globals is destroyed
		Class<AbstractCustomModule> clazz = (Class<AbstractCustomModule>) classLoader.loadClass(className);
		globals.registerDynamicClass(clazz);
		return clazz;
	}

	private AbstractCustomModule setUpCustomModule(Class<AbstractCustomModule> clazz, Map config)
			throws InstantiationException, IllegalAccessException {
		// Create & init instance
		AbstractCustomModule instance = clazz.newInstance();
		instance.init();

		for (Input it : instance.getInputs()) {
			addInput(it);
		}

		for (Output it : instance.getOutputs()) {
			addOutput(it);
		}

		// Inject stuff into the module
		instance.globals = globals;
		instance.setHash(hash);
		instance.setParentSignalPath(parentSignalPath);
		instance.configure(config);

		return instance;
	}

	@Override
	public void beforeSerialization() {
		super.beforeSerialization();

		// Ensure that there are no links to AbstractCustomModule before serialization so that it not serialized by
		// virtue of belonging to object graph. Note that <code>instance</code> is already defined as transient.
		changeOwnerOfEndpoints(null);

		storedEndpointFields = StoredEndpointFields.clearAndCollect(instance);

		// Note: instance.beforeSerialization() invoked indirectly
		serializedInstance = serializationService().serialize(instance);
	}

	@Override
	public void afterDeserialization() {
		super.afterDeserialization();
		try {
			compileAndRegister(className, fullCode);
			instance = (AbstractCustomModule) serializationService().deserialize(serializedInstance, classLoader);

			instance.copyStateFromWrapper(parentSignalPath,
					inputs,
					inputsByName,
					outputs,
					outputsByName,
					drivingInputs,
					globals);


			storedEndpointFields.restoreFields(instance);
			storedEndpointFields = null;

			changeOwnerOfEndpoints(instance);

		} catch (ClassNotFoundException e) {
			throw new RuntimeException("Exception " + e);
		}
	}

	private void changeOwnerOfEndpoints(AbstractSignalPathModule newOwner) {
		for (Input in : getInputs()) {
			in.setOwner(newOwner);
		}
		for (Output out : getOutputs()) {
			out.setOwner(newOwner);
		}
	}

	private SerializationService serializationService() {
		return globals.getSerializationService();
	}
}
