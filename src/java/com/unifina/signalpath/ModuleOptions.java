package com.unifina.signalpath;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Holder map for ModuleOption instances
 */
public class ModuleOptions extends LinkedHashMap<String, Object> {
	
	public static ModuleOptions get(Map<String, Object> parentConfig) {
		if (parentConfig.containsKey("options")) {
			Map options = (Map) parentConfig.get("options");
			if (options instanceof ModuleOptions) {
				return (ModuleOptions) options;
			} else {
				ModuleOptions mo = new ModuleOptions();
				mo.putAll(options);
				parentConfig.put("options", mo);
				return mo;
			}
		} else {
			ModuleOptions mo = new ModuleOptions();
			parentConfig.put("options", mo);
			return mo;
		}
	}
	
	public ModuleOption getOption(String key) {
		return containsKey(key) ? ModuleOption.get(key, (Map) get(key), this) : null;
	}
	
	public void add(ModuleOption option) {
		this.put(option.getKey(), option);
	}

	public void addIfMissing(ModuleOption option) {
		if (!containsKey(option.getKey())) {
			add(option);
		}
	}
	
}
