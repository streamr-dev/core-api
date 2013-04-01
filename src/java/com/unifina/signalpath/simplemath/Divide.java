package com.unifina.signalpath.simplemath;

import com.unifina.signalpath.AbstractSignalPathModule;
import com.unifina.signalpath.TimeSeriesInput;
import com.unifina.signalpath.TimeSeriesOutput;

public class Divide extends AbstractSignalPathModule {

	TimeSeriesInput a = new TimeSeriesInput(this,"A");
	TimeSeriesInput b = new TimeSeriesInput(this,"B");
	
	TimeSeriesOutput out = new TimeSeriesOutput(this,"A/B");
	
	@Override
	public void init() {
		addInput(a);
		addInput(b);
		addOutput(out);
	}
	
	public void clearState() {
		
	}
	
	public void sendOutput() {
		if (b.value != 0)
			out.send(a.value / b.value);
	}
	
}
