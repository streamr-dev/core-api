package com.unifina.signalpath.simplemath;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.unifina.signalpath.AbstractSignalPathModule;
import com.unifina.signalpath.TimeSeriesInput;
import com.unifina.signalpath.TimeSeriesOutput;

public class AddMulti extends AbstractSignalPathModule {

	int inputCount = 2;
	ArrayList<TimeSeriesInput> inputList = new ArrayList<>();
	TimeSeriesOutput out = new TimeSeriesOutput(this,"sum");
	
	@Override
	public void init() {
		addOutput(out);
	}
	
	public void clearState() {
		
	}
	
	public void sendOutput() {
		double sum = 0;
		for (TimeSeriesInput i : inputList)
			sum += i.value;
		
		out.send(sum);
	}
	
	@Override
	public Map<String,Object> getConfiguration() {
		Map<String,Object> config = super.getConfiguration();

		Map<String,Object> optionsMap = new HashMap<>();
		
		Map<String,Object> inputsMap = new HashMap<>();
		inputsMap.put("value", inputCount);
		inputsMap.put("type", "int");
		optionsMap.put("inputs", inputsMap);
		
		config.put("options",optionsMap);
		return config;
	}
	
	@Override
	public void onConfiguration(Map<String,Object> config) {
		super.onConfiguration(config);
		
		Map options = (Map) config.get("options");
		
		if (options!=null) {
			inputCount = (int) ((Map)options.get("inputs")).get("value");
		}
		
		for (int p=1;p<=inputCount;p++) {
			TimeSeriesInput input = new TimeSeriesInput(this,"in"+p);
			addInput(input);
			inputList.add(input);
		}
	}
	
}
