package com.unifina.signalpath.custom;

import com.google.gson.Gson;
import org.apache.log4j.Logger;

import javax.script.*;
import java.io.Serializable;
import java.util.*;

class JavaScriptEngine {
	private static final Logger log = Logger.getLogger(JavaScriptEngine.class);

	private final ScriptEngine scriptEngine = new ScriptEngineManager().getEngineByExtension("js");
	private final Gson gson = new Gson();
	private final List<String> functionNames = new ArrayList<>();
	private final List<Variable> variables = new ArrayList<>();

	JavaScriptEngine(String code) {
		evaluate(code);
		analyzeIdentifiers();
	}

	void define(String name, Object value) {
		scriptEngine.put(name, javaToJs(value));
	}

	Object invoke(String functionName, Object... args) {
		for (int i=0; i < args.length; ++i) {
			args[i] = javaToJs(args[i]);
		}
		try {
			Object returnValue = ((Invocable) scriptEngine).invokeFunction(functionName, args);
			return jsToJava(returnValue);
		} catch (ScriptException e) {
			throw new JavaScriptException(e);
		} catch (NoSuchMethodException e) {
			throw new JavaScriptException(e);
		}
	}

	List<String> getFunctionNames() {
		return Collections.unmodifiableList(functionNames);
	}

	List<Variable> getVariables() {
		return Collections.unmodifiableList(variables);
	}

	Map<String, Object> getState() {
		Map<String, Object> stateWithoutFunctions = new HashMap<>();
		for (Map.Entry<String, Object> entry : getGlobalScopeIdentifiers().entrySet()) {
			if (!isFunction(entry.getValue()) && !isContextObject(entry.getValue())) {
				stateWithoutFunctions.put(entry.getKey(), jsToJava(entry.getValue()));
			}
		}
		return stateWithoutFunctions;
	}

	private void evaluate(String code) {
		try {
			scriptEngine.eval(code);
		} catch (ScriptException e) {
			throw new JavaScriptException(e);
		}
	}

	private void analyzeIdentifiers() {
		for (Map.Entry<String, Object> entry : getGlobalScopeIdentifiers().entrySet()) {
			String identifier = entry.getKey();
			Object defaultValue = jsToJava(entry.getValue());

			if (defaultValue instanceof Boolean) {
				variables.add(new Variable(identifier, Boolean.class, defaultValue));
			} else if (defaultValue instanceof Double) {
				variables.add(new Variable(identifier, Double.class, defaultValue));
			} else if (defaultValue instanceof String) {
				variables.add(new Variable((identifier), String.class, defaultValue));
			} else if (defaultValue == null) {
				variables.add(new Variable((identifier), Object.class, defaultValue));
			} else if (defaultValue instanceof List) {
				variables.add(new Variable((identifier), List.class, defaultValue));
			} else if (defaultValue instanceof Map) {
				variables.add(new Variable((identifier), Map.class, defaultValue));
			} else if (isFunction(defaultValue)) {
				functionNames.add(identifier);
			} else if (!isContextObject(defaultValue)) {
				log.warn("Unidentified identifier '" + identifier + "' with value '" + defaultValue + "'.");
			}
		}
	}

	private Bindings getGlobalScopeIdentifiers() {
		return scriptEngine.getBindings(ScriptContext.ENGINE_SCOPE);
	}

	private static boolean isFunction(Object o) {
		return o.getClass().toString().toLowerCase().contains("function");
	}

	private static boolean isContextObject(Object o) {
		return o instanceof SimpleScriptContext;
	}

	private Object javaToJs(Object value) {
		if (value instanceof List || value instanceof Map) {
			try {
				String json = gson.toJson(value);
				// Self-executing anonymous function because eval doesn't work with object literals
				String script = String.format("(function() { return %s; })()", json);
				return scriptEngine.eval(script);
			} catch (ScriptException e) {
				throw new JavaScriptException(e);
			}
		}
		return value;
	}

	/**
	 * Converts JavaScript object to equivalent Java object
	 */
	private static Object jsToJava(Object value) {
		if (value instanceof Map) {
			Map map = new HashMap();
			for (Object key : ((Map) value).keySet()) {
				map.put(key, jsToJava(((Map) value).get(key)));
			}
			return map;
		} else if (value instanceof List) {
			List list = new ArrayList();
			for (Object o : (List) value) {
				list.add(jsToJava(o));
			}
			return list;
		}
		return value;
	}

	static class Variable implements Serializable {
		private final String name;
		private final Class clazz;
		private final Object defaultValue;

		Variable(String name, Class type, Object defaultValue) {
			this.name = name;
			this.clazz = type;
			this.defaultValue = defaultValue;
		}

		public String getName() {
			return name;
		}

		public Class getClazz() {
			return clazz;
		}

		public Object getDefaultValue() {
			return defaultValue;
		}

		@Override
		public String toString() {
			return "Variable{" +
				"name='" + name + '\'' +
				", clazz=" + clazz.getSimpleName() +
				", defaultValue=" + defaultValue +
				'}';
		}
	}
}
