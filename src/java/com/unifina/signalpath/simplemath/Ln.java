package com.unifina.signalpath.simplemath;

import com.unifina.signalpath.AbstractSignalPathModule;
import com.unifina.signalpath.TimeSeriesInput;
import com.unifina.signalpath.TimeSeriesOutput;

public class Ln extends AbstractSignalPathModule {

	TimeSeriesInput input = new TimeSeriesInput(this,"in");
	TimeSeriesOutput out = new TimeSeriesOutput(this,"out");
	
	@Override
	public void init() {
		addInput(input);
		addOutput(out);
	}

	@Override
	public void sendOutput() {
		if (input.value>0)
			out.send(Math.log(input.value));
	}

	@Override
	public void clearState() {
	}

}
