package com.unifina.signalpath.filtering;

import java.util.List;
import java.util.Map;

import com.unifina.math.MovingAverage;
import com.unifina.signalpath.AbstractSignalPathModule;
import com.unifina.signalpath.IntegerParameter;
import com.unifina.signalpath.TimeSeriesInput;
import com.unifina.signalpath.TimeSeriesOutput;

@Deprecated
public class SimpleMovingAverageEvents extends AbstractSignalPathModule {
	
	IntegerParameter length = new IntegerParameter(this,"length",60);
	IntegerParameter minSamples = new IntegerParameter(this,"minSamples",1);
	
	TimeSeriesInput input = new TimeSeriesInput(this,"in");
	TimeSeriesOutput out = new TimeSeriesOutput(this,"out");
	
	MovingAverage ma;
	
	@Override
	public void init() {
		addInput(length);
		addInput(minSamples);
		addInput(input);
		addOutput(out);
	}
	
	@Override
	public void sendOutput() {
		if (ma==null)
			ma = new MovingAverage(length.getValue());
		
		if (ma.getLength()!=length.getValue())
			ma.setLength(length.getValue());
			
		ma.add(input.value);
		if (ma.size()>=minSamples.getValue())
			out.send(ma.getValue());
	}
	
	@Override
	public void clearState() {
		if (ma != null)
			ma.clear();
	}
	
	@Override
	protected void onConfiguration(Map<String, Object> config) {
		super.onConfiguration(config);
		
		// This is for backwards compatibility! If the config contains no value for "minSamples",
		// use the value of length (if found). TODO: remove some day
		boolean minSamplesFound = false;
		boolean lengthFound = false;
		if (config.containsKey("params")) {
			for (Map param : (List<Map>) config.get("params")) {
				if (param.get("name").equals("minSamples"))
					minSamplesFound = true;
				else if (param.get("name").equals("length"))
					minSamplesFound = true;
			}
		}
		
		if (lengthFound && !minSamplesFound && !minSamples.isConnected() && !length.isConnected())
			minSamples.receive(length.value);
	}
	
}
