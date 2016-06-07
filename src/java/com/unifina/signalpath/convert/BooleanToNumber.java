package com.unifina.signalpath.convert;

import com.unifina.signalpath.AbstractSignalPathModule;
import com.unifina.signalpath.BooleanInput;
import com.unifina.signalpath.TimeSeriesOutput;


public class BooleanToNumber extends AbstractSignalPathModule {

	BooleanInput in = new BooleanInput(this, "in");

	TimeSeriesOutput out = new TimeSeriesOutput(this, "out");

	@Override
	public void sendOutput() {
		if (in.getValue()) {
			out.send(1d);
		} else {
			out.send(0d);
		}
	}

	@Override
	public void clearState() {

	}
}
