package com.unifina.signalpath;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Holder map for ModuleOption instances
 */
public class ModuleOptions extends LinkedHashMap<String, Object> {
	
	public ModuleOptions() {}
	
	public static ModuleOptions get(Map<String, Object> parentConfig) {
		ModuleOptions mo;
		if (parentConfig.containsKey("options")) {
			Map options = (Map) parentConfig.get("options");
			if (options instanceof ModuleOptions)
				return (ModuleOptions) options;
			else {
				mo = new ModuleOptions();
				mo.putAll(options);
			}
		}
		else {
			mo = new ModuleOptions();
		}
		parentConfig.put("options", mo);
		return mo;
	}
	
	public ModuleOption getOption(String key) {
		if (this.containsKey(key))
			return ModuleOption.get(key, (Map)this.get(key), this);
		else return null;
	}
	
	public void add(ModuleOption option) {
		this.put(option.getKey(), option);
	}

	public void addIfMissing(ModuleOption option) {
		if (!this.containsKey(option.getKey())) {
			this.put(option.getKey(), option);
		}
	}
	
}
