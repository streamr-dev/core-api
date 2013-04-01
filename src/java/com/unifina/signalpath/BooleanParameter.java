package com.unifina.signalpath;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BooleanParameter extends Parameter<Boolean> {
	
    private static final List<Map<String,String>> possibleValues = new ArrayList<Map<String,String>>();
    static {
        Map<String, String> f = new HashMap<String,String>();
        f.put("name", "false");
        f.put("value", "false");
        possibleValues.add(Collections.unmodifiableMap(f));
        
        Map<String, String> t = new HashMap<String,String>();
        t.put("name", "true");
        t.put("value", "true");
        possibleValues.add(Collections.unmodifiableMap(t));
    }

	public BooleanParameter(AbstractSignalPathModule owner, String name,
			Boolean defaultValue) {
		super(owner, name, defaultValue, "Boolean");
	}

	@Override
	public Map<String,Object> getConfiguration() {
		Map<String,Object> config = super.getConfiguration();
		config.put("possibleValues",possibleValues);
		return config;
	}

	@Override
	Boolean parseValue(String s) {
		return Boolean.parseBoolean(s);
	}
	
}
