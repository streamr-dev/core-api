package com.unifina.signalpath.bool;

import com.unifina.signalpath.*;

public class Equals extends AbstractSignalPathModule {

	DoubleParameter tolerance = new DoubleParameter(this,"tolerance",0D);
	TimeSeriesInput a = new TimeSeriesInput(this,"A");
	TimeSeriesInput b = new TimeSeriesInput(this,"B");
	
	BooleanOutput out = new BooleanOutput(this,"out");
	
	@Override
	public void init() {
		addInput(tolerance);
		addInput(a);
		addInput(b);
		addOutput(out);
	}
	
	public void clearState() {
		
	}
	
	public void sendOutput() {
		out.send(Math.abs(a.value - b.value) <= tolerance.getValue());
	}
	
}
