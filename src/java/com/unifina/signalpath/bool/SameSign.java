package com.unifina.signalpath.bool;

import com.unifina.signalpath.AbstractSignalPathModule;
import com.unifina.signalpath.BooleanOutput;
import com.unifina.signalpath.TimeSeriesInput;
import com.unifina.signalpath.TimeSeriesOutput;

public class SameSign extends AbstractSignalPathModule {

	TimeSeriesInput a = new TimeSeriesInput(this,"A");
	TimeSeriesInput b = new TimeSeriesInput(this,"B");
	
	BooleanOutput out = new BooleanOutput(this,"sameSign");
	TimeSeriesOutput sign = new TimeSeriesOutput(this,"sign");

	@Override
	public void init() {
		addInput(a);
		addInput(b);
		addOutput(out);
		addOutput(sign);
	}
	
	public void clearState() {
		
	}
	
	public void sendOutput() {
		if (Math.signum(a.value) == Math.signum(b.value)) {
			out.send(true);
			sign.send(Math.signum(a.value));
		} else {
			out.send(false);
		}
	}
	
}
