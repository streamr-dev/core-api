package com.unifina.signalpath.bool;

import com.unifina.signalpath.AbstractSignalPathModule;
import com.unifina.signalpath.TimeSeriesInput;
import com.unifina.signalpath.TimeSeriesOutput;

public class Or extends AbstractSignalPathModule {

	TimeSeriesInput a = new TimeSeriesInput(this,"A");
	TimeSeriesInput b = new TimeSeriesInput(this,"B");
	
	TimeSeriesOutput out = new TimeSeriesOutput(this,"out");
	
	@Override
	public void init() {
		addInput(a);
		addInput(b);
		addOutput(out);
	}
	
	public void clearState() {
		
	}
	
	public void sendOutput() {
		if (a.value==1D || b.value==1D)
			out.send(1D);
		else out.send(0D);
	}
	
}
