package com.unifina.signalpath.utils;

import com.unifina.signalpath.AbstractSignalPathModule;
import com.unifina.signalpath.TimeSeriesInput;
import com.unifina.signalpath.TimeSeriesOutput;

public class PassThrough extends AbstractSignalPathModule {

	TimeSeriesInput input = new TimeSeriesInput(this,"in");
	TimeSeriesOutput output = new TimeSeriesOutput(this,"out");
	
	@Override
	public void init() {
		addInput(input);
		addOutput(output);
	}
	
	@Override
	public void sendOutput() {
		output.send(input.value);
	}

	@Override
	public void clearState() {

	}

}
