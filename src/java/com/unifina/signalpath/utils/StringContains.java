package com.unifina.signalpath.utils;

import java.util.HashMap;
import java.util.Map;

import com.unifina.signalpath.AbstractSignalPathModule;
import com.unifina.signalpath.StringInput;
import com.unifina.signalpath.StringParameter;
import com.unifina.signalpath.TimeSeriesOutput;
import com.unifina.utils.MapTraversal;

public class StringContains extends AbstractSignalPathModule {

	StringParameter s = new StringParameter(this,"search","");
	StringInput in = new StringInput(this,"string");
	TimeSeriesOutput out = new TimeSeriesOutput(this,"contains");
	
	boolean ignoreCase = true;
	
	@Override
	public void init() {
		addInput(s);
		addInput(in);
		addOutput(out);
	}

	@Override
	public void sendOutput() {
		// Output nothing if searching for an empty string
		if (!s.getValue().isEmpty()) {
			if (!ignoreCase && in.getValue().contains(s.getValue())
					|| ignoreCase && in.getValue().toLowerCase().contains(s.getValue().toLowerCase()))
				out.send(1);
			else out.send(0);
		}
	}

	@Override
	public void clearState() {

	}

	@Override
	public Map<String, Object> getConfiguration() {
		Map<String,Object> config = super.getConfiguration();
		
		if (!config.containsKey("options")) {
			config.put("options", new HashMap<String,Object>());
		}
		Map<String,Object> options = (Map<String,Object>) config.get("options");
		
		Map<String,Object> caseOption = new HashMap<>();
		options.put("ignoreCase", caseOption);
		caseOption.put("type","boolean");
		caseOption.put("value",ignoreCase);
		
		return config;
	}
	
	@Override
	protected void onConfiguration(Map<String, Object> config) {
		super.onConfiguration(config);
		if (MapTraversal.getProperty(config, "options.ignoreCase.value")!=null) {
			ignoreCase = MapTraversal.getBoolean(config, "options.ignoreCase.value"); 
		}
	}
	
}
