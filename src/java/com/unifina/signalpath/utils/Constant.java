package com.unifina.signalpath.utils;

import com.unifina.signalpath.AbstractSignalPathModule;
import com.unifina.signalpath.DoubleParameter;
import com.unifina.signalpath.Input;
import com.unifina.signalpath.IntegerParameter;
import com.unifina.signalpath.Output;
import com.unifina.signalpath.Pullable;
import com.unifina.signalpath.TimeSeriesInput;
import com.unifina.signalpath.TimeSeriesOutput;

public class Constant extends AbstractSignalPathModule implements Pullable<Double> {

	DoubleParameter constant = new DoubleParameter(this, "constant", 0D);
	TimeSeriesOutput out = new TimeSeriesOutput(this, "out");

	public Constant() {
		super();
//		originatingModule = true;
		initPriority = 40;
	}

	@Override
	public void init() {
		addInput(constant);
		constant.setDrivingInput(true);
		addOutput(out);
	}

	@Override
	public void initialize() {
		for (Input i : out.getTargets()) {
			if (i instanceof TimeSeriesInput) {
				((TimeSeriesInput) i).setInitialValue(constant.getValue());
			} else if (i instanceof IntegerParameter) {
				((IntegerParameter) i).receive(constant.getValue().intValue());
			} else {
				i.receive(constant.getValue());
			}
		}
	}

	@Override
	public void sendOutput() {
		out.send(constant.getValue());
	}

	@Override
	public void clearState() {

	}

	@Override
	public Double pullValue(Output output) {
		return constant.getValue();
	}

}
