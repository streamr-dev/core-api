package com.unifina.signalpath.trigger;

import com.unifina.signalpath.AbstractSignalPathModule;
import com.unifina.signalpath.DoubleParameter;
import com.unifina.signalpath.TimeSeriesInput;
import com.unifina.signalpath.TimeSeriesOutput;

public class Scaler extends AbstractSignalPathModule {

	DoubleParameter triggerTick = new DoubleParameter(this, "triggerTick", 1D);
	DoubleParameter releaseTick = new DoubleParameter(this, "releaseTick", -1D);
	DoubleParameter zero = new DoubleParameter(this, "zero", 0D);

	TimeSeriesInput input = new TimeSeriesInput(this, "in");
	TimeSeriesOutput out = new TimeSeriesOutput(this, "out");
	
	@Override
	public void init() {
		addInput(triggerTick);
		addInput(releaseTick);
		addInput(zero);
		addInput(input);
		addOutput(out);
	}

	@Override
	public void sendOutput() {
		// TODO Auto-generated method stub

	}

	@Override
	public void clearState() {
		// TODO Auto-generated method stub

	}

}
