package com.unifina.signalpath.custom;

import com.unifina.service.SerializationService;
import com.unifina.signalpath.*;
import org.apache.log4j.Logger;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.*;

public class JavaScriptModule extends ModuleWithUI {
	private static final Logger log = Logger.getLogger(JavaScriptModule.class);

	private static final String DEFAULT_CODE = "function main() {\n\treturn 0;\n}\n";

	private final StringParameter function = new StringParameter(this, "function", "main") {
		@Override
		protected List<PossibleValue> getPossibleValues() {
			List<PossibleValue> possibleValues = new ArrayList<>();
			if (jsEngine != null) {
				for (String name : jsEngine.getFunctionNames()) {
					possibleValues.add(new PossibleValue(name, name));
				}
			}
			return possibleValues;
		}
	};
	private final ListInput arguments = new ListInput(this, "arguments");
	private final Output<Object> returnValue = new Output<>(this, "return", "Object");

	private final SimpleDateFormat debugDf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
	private transient JavaScriptEngine jsEngine;
	private List<Input> variableInputs;
	private String code = DEFAULT_CODE;
	private Map<String, Object> serializedJavaScriptState;

	@Override
	public void init() {
		addInput(function);
		addInput(arguments);
		addOutput(returnValue);
	}

	@Override
	public Map<String, Object> getConfiguration() {
		Map<String, Object> config = super.getConfiguration();
		config.put("code", code);
		config.put("language", "javascript");
		return config;
	}

	@Override
	protected void onConfiguration(Map<String, Object> config) {
		super.onConfiguration(config);

		if (config.containsKey("code")) {
			code = (String) config.get("code");
		}

		// Initialize JavaScript
		clearState();

		variableInputs = new ArrayList<>();
		for (JavaScriptEngine.Variable var : jsEngine.getVariables()) {

			String name = var.getName();
			Class clazz = var.getClazz();
			Object defaultValue = var.getDefaultValue();

			Input input;

			if (clazz.equals(Boolean.class)) {
				input = new BooleanParameter(this, name, (Boolean) defaultValue);
			} else if (clazz.equals(Double.class)) {
				input = new DoubleParameter(this, name, (Double) defaultValue);
			} else if (clazz.equals(String.class)) {
				input = new StringParameter(this, name, (String) defaultValue);
			} else if (clazz.equals(Object.class)) {
				input = new Input<>(this, name, "Object");
			} else if (clazz.equals(List.class)) {
				input = new ListInput(this, name);
			} else if (clazz.equals(Map.class)) {
				input = new MapInput(this, name);
			} else {
				throw new RuntimeException("Unexpected variable class" + clazz);
			}

			input.requiresConnection = false;
			input.canToggleDrivingInput = false;
			input.setDrivingInput(true);
			input.setDisplayName(String.format("%s (%s)", input.getName(), input.getTypeName().toLowerCase()));
			input.setReadyHack();
			variableInputs.add(input);
			addInput(input);
		}
	}

	@Override
	public void sendOutput() {
		for (Input input : variableInputs) {
			if (drivingInputs.contains(input)) {
				jsEngine.define(input.getName(), input.getValue());
			}
		}
		try {
			Object result = jsEngine.invoke(function.getValue(), arguments.getValue().toArray());
			if (result != null) {
				returnValue.send(result);
			}
		} catch (JavaScriptException e) {
			log.warn(e.getMessage());
			sendDebugMessage(e.getMessage());
			pushToUiChannel(new RuntimeErrorMessage(e.lineNumber, e.getMessage()));
		}
	}

	@Override
	public void clearState() {
		try {
			jsEngine = new JavaScriptEngine(code);
		} catch (JavaScriptException e) {
			CompilationErrorMessage msg = new CompilationErrorMessage(e.lineNumber, e.getMessage());
			throw new ModuleException(e, new ModuleExceptionMessage(getHash(), msg));
		}
		jsEngine.define("system", new System());
	}

	@Override
	public void beforeSerialization() {
		super.beforeSerialization();
		serializedJavaScriptState = jsEngine.getState();
	}

	@Override
	public void afterDeserialization(SerializationService serializationService) {
		super.afterDeserialization(serializationService);
		clearState();
		for (Map.Entry<String, Object> entry : serializedJavaScriptState.entrySet()) {
			jsEngine.define(entry.getKey(), entry.getValue());
		}
		serializedJavaScriptState = null;
	}

	private void sendDebugMessage(Object msg) {
		Map<String, String> uiMsg = new HashMap<>();
		uiMsg.put("type", "debug");
		uiMsg.put("msg", msg.toString());
		uiMsg.put("t", debugDf.format(getGlobals().time));
		pushToUiChannel(uiMsg);
	}

	/**
	 * Functions that are invocable from JavaScript.
	 */
	public class System implements Serializable {
		public double time() {
			return getGlobals().time.getTime();
		}

		public void debug(Object msg) {
			sendDebugMessage(msg);
		}
	}
}
