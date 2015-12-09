package com.unifina.signalpath;

import java.util.LinkedHashMap;
import java.util.Map;

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
		if (o!=null)
			return o.toString();
		else return null;
	}
	
	public Integer getInt() {
		Object o = getValue();
		if (o instanceof Integer)
			return (Integer) o;
		else return Integer.parseInt(getString());
	}
	
	public Double getDouble() {
		Object o = getValue();
		if (o instanceof Double)
			return (Double) o;
		else return Double.parseDouble(getString());
	}
	
	public Boolean getBoolean() {
		Object o = getValue();
		if (o instanceof Boolean)
			return (Boolean) o;
		else return Boolean.parseBoolean(getString());
	}
	
}
