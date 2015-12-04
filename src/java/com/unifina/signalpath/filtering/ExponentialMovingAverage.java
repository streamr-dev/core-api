package com.unifina.signalpath.filtering;

import com.unifina.signalpath.AbstractSignalPathModule;
import com.unifina.signalpath.IntegerParameter;
import com.unifina.signalpath.TimeSeriesInput;
import com.unifina.signalpath.TimeSeriesOutput;

public class ExponentialMovingAverage extends AbstractSignalPathModule {

	com.unifina.math.ExponentialMovingAverage ema;
	
	IntegerParameter length = new IntegerParameter(this,"length",60);
	IntegerParameter minSamples = new IntegerParameter(this,"minSamples",1);
	
	TimeSeriesInput input = new TimeSeriesInput(this,"in");
	TimeSeriesOutput out = new TimeSeriesOutput(this,"out");
	
	@Override
	public void init() {
		addInput(length);
		addInput(minSamples);
		addInput(input);
		addOutput(out);
	}
	
	@Override
	public void sendOutput() {
		if (ema==null)
			ema = new com.unifina.math.ExponentialMovingAverage(length.value);
		
		ema.setLength(length.value);
		ema.add(input.value);
		if (ema.size()>=minSamples.value)
			out.send(ema.getValue());
	}
	
	@Override
	public void clearState() {
		if (ema!=null)
			ema.clear();
	}

}
