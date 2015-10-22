package com.unifina.signalpath.simplemath;

import com.unifina.signalpath.AbstractSignalPathModule;
import com.unifina.signalpath.IntegerParameter;
import com.unifina.signalpath.TimeSeriesInput;
import com.unifina.signalpath.TimeSeriesOutput;

public class Modulo extends AbstractSignalPathModule {

	IntegerParameter n = new IntegerParameter(this,"divisor", 2);
	
	TimeSeriesInput a = new TimeSeriesInput(this,"dividend");
	
	TimeSeriesOutput r = new TimeSeriesOutput(this,"remainder");
	
	@Override
	public void init() {
		addInput(a);
		addInput(n);
		addOutput(r);
	}
	
	public void clearState() {
		
	}
	
	public void sendOutput() {
		r.send(a.getValue() % n.getValue());
	}
	
}
