package com.unifina.signalpath.simplemath;

import com.unifina.signalpath.AbstractSignalPathModule;
import com.unifina.signalpath.TimeSeriesInput;
import com.unifina.signalpath.TimeSeriesOutput;

public class SquareRoot extends AbstractSignalPathModule {

	TimeSeriesInput input = new TimeSeriesInput(this, "in");
	TimeSeriesOutput sqrt = new TimeSeriesOutput(this, "sqrt");
	
	@Override
	public void sendOutput() {
		sqrt.send(Math.sqrt(input.getValue()));
	}

	@Override
	public void clearState() {
		
	}

}
