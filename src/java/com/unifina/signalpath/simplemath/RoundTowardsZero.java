package com.unifina.signalpath.simplemath;

import com.unifina.signalpath.AbstractSignalPathModule;
import com.unifina.signalpath.IntegerParameter;
import com.unifina.signalpath.TimeSeriesInput;
import com.unifina.signalpath.TimeSeriesOutput;

/**
 * Replaced by RoundToStep
 */
@Deprecated
public class RoundTowardsZero extends AbstractSignalPathModule {

	IntegerParameter precision = new IntegerParameter(this,"precision",0);
	TimeSeriesInput input = new TimeSeriesInput(this,"in");
	TimeSeriesOutput out = new TimeSeriesOutput(this,"out");
	
	@Override
	public void init() {
		addInput(precision);
		addInput(input);
		addOutput(out);
	}
	
	@Override
	public void sendOutput() {
		double temp = input.value * Math.pow(10, precision.getValue());
		temp = (input.value>=0 ? Math.floor(temp) : Math.ceil(temp));
		temp = temp / Math.pow(10, precision.getValue());
		out.send(temp);
	}

	@Override
	public void clearState() {

	}

}
