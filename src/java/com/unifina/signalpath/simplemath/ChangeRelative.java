package com.unifina.signalpath.simplemath;

import com.unifina.signalpath.AbstractSignalPathModule;
import com.unifina.signalpath.TimeSeriesInput;
import com.unifina.signalpath.TimeSeriesOutput;

public class ChangeRelative extends AbstractSignalPathModule {

	TimeSeriesInput input = new TimeSeriesInput(this,"in");
	TimeSeriesOutput out = new TimeSeriesOutput(this,"out");
	
	Double prev = null;
	
	@Override
	public void init() {
		addInput(input);
		addOutput(out);
	}
	
	public void clearState() {
		prev = null;
	}
	
	public void sendOutput() {
		if (input.value!=null && prev != null && prev != 0)
			out.send((input.value-prev)/prev);
		
		prev = input.value;
	}
	
}
