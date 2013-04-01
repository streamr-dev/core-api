package com.unifina.signalpath.filtering;

import com.unifina.signalpath.AbstractSignalPathModule;
import com.unifina.signalpath.DoubleParameter;
import com.unifina.signalpath.IntegerParameter;
import com.unifina.signalpath.TimeSeriesInput;
import com.unifina.signalpath.TimeSeriesOutput;

public class ExponentialMovingAverage extends AbstractSignalPathModule {

	Double ema = null;
	int samples = 0;
	
	DoubleParameter alpha = new DoubleParameter(this,"alpha",0.01);
	IntegerParameter minSamples = new IntegerParameter(this,"minSamples",1);
	
	TimeSeriesInput input = new TimeSeriesInput(this,"in");
	TimeSeriesOutput out = new TimeSeriesOutput(this,"out");
	
	@Override
	public void init() {
		addInput(alpha);
		addInput(input);
		addOutput(out);
	}
	
	@Override
	public void sendOutput() {
		if (ema==null) {
			ema = input.value;
			samples = 1;
			
			if (samples >= minSamples.getValue())
				out.send(ema);
		}
		else {
			ema = alpha.getValue() * input.value + (1-alpha.getValue()) * ema;
			samples++;
			
			if (samples >= minSamples.getValue())
				out.send(ema);
		}
	}

	@Override
	public void clearState() {
		ema = null;
		samples = 0;
	}

}
