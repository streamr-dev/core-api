package com.unifina.signalpath;

import java.util.*;

public class ModuleOption extends LinkedHashMap<String, Object> {

	public static final String OPTION_STRING = "string";
	public static final String OPTION_INTEGER = "int";
	public static final String OPTION_BOOLEAN = "boolean";
	public static final String OPTION_DOUBLE = "double";

	private String key;

	static ModuleOption get(String key, Map<String, Object> map, ModuleOptions options) {
		if (map instanceof ModuleOption)
			return (ModuleOption) map;
		else {
			ModuleOption mo = new ModuleOption(key, map);
			options.add(mo);
			return mo;
		}
	}

	// Required for serialization
	@SuppressWarnings("unused")
	public ModuleOption() {}
	
	ModuleOption(String key, Map<String, Object> map) {
		this.key = key;
		this.putAll(map);
	}
	
	public ModuleOption(String key, Object value, String type) {
		this.key = key;
		this.put("value", value);
		this.put("type", type);
	}

	/**
	 * List of possible values will be rendered as dropdown box
	 * @param description shown to user
	 * @param value written into canvas JSON and sent back from UI
     */
	public void addPossibleValue(String description, String value) {
		List<Map<String, String>> choices = (List<Map<String, String>>)this.get("possibleValues");
		if (choices == null) {
			choices = new LinkedList<>();
			this.put("possibleValues", choices);
		}
		Map<String, String> choice = new HashMap<>();
		choice.put("text", description);
		choice.put("value", value);
		choices.add(choice);
	}

	public void addTo(Map<String,Object> options) {
		options.put(key, this);
	}
	
	public String getKey() {
		return key;
	}
	
	public Object getValue() {
		return this.get("value");
	}
	
	public String getString() {
		Object o = getValue();
		return (o != null) ? o.toString() : null;
	}
	
	public Integer getInt() {
		Object o = getValue();
		return (o instanceof Integer) ? (Integer) o : Integer.parseInt(getString());
	}
	
	public Double getDouble() {
		Object o = getValue();
		return (o instanceof Double) ? (Double) o : Double.parseDouble(getString());
	}
	
	public Boolean getBoolean() {
		Object o = getValue();
		return (o instanceof Boolean) ? (Boolean) o : Boolean.parseBoolean(getString());
	}

	public static ModuleOption createBoolean(String key, Boolean value) {
		return new ModuleOption(key, value, OPTION_BOOLEAN);
	}

	public static ModuleOption createInt(String key, Integer value) {
		return new ModuleOption(key, value, OPTION_INTEGER);
	}

	public static ModuleOption createString(String key, String value) {
		return new ModuleOption(key, value, OPTION_STRING);
	}
}
