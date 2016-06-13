package com.unifina.signalpath.simplemath;

import java.util.HashMap;
import java.util.Map;

import com.unifina.signalpath.AbstractSignalPathModule;
import com.unifina.signalpath.TimeSeriesInput;
import com.unifina.signalpath.TimeSeriesOutput;

@Deprecated
public class AddMulti extends AbstractSignalPathModule {

	int multiInputCount = 2;
	TimeSeriesInput[] inputArr = new TimeSeriesInput[0];
	TimeSeriesOutput out = new TimeSeriesOutput(this,"sum");
	
	@Override
	public void init() {
		addOutput(out);
	}
	
	public void clearState() {
		
	}
	
	public void sendOutput() {
		double sum = 0;
		for (int i=0;i<inputArr.length;i++)
			sum += inputArr[i].value;
		out.send(sum);
	}
	
	@Override
	public Map<String,Object> getConfiguration() {
		Map<String,Object> config = super.getConfiguration();

		Map<String,Object> optionsMap = new HashMap<>();
		
		Map<String,Object> inputsMap = new HashMap<>();
		inputsMap.put("value", multiInputCount);
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
			multiInputCount = (int) ((Map)options.get("inputs")).get("value");
		}
		
		inputArr = new TimeSeriesInput[multiInputCount];
		for (int p=1;p<= multiInputCount;p++) {
			TimeSeriesInput input = new TimeSeriesInput(this,"in"+p);
			addInput(input);
			inputArr[p-1] = input;
		}
	}
	
}
