package com.unifina.signalpath.bool;

import com.unifina.signalpath.AbstractSignalPathModule;
import com.unifina.signalpath.BooleanParameter;
import com.unifina.signalpath.TimeSeriesInput;
import com.unifina.signalpath.TimeSeriesOutput;

public class GreaterThan extends AbstractSignalPathModule {

	BooleanParameter equality = new BooleanParameter(this,"equality",false);
	
	TimeSeriesInput a = new TimeSeriesInput(this,"A");
	TimeSeriesInput b = new TimeSeriesInput(this,"B");
	
	TimeSeriesOutput out = new TimeSeriesOutput(this,"A&gt;B");
	
	@Override
	public void init() {
		addInput(equality);
		addInput(a);
		addInput(b);
		addOutput(out);
	}
	
	public void clearState() {
		
	}
	
	public void sendOutput() {
		if (a.value > b.value || equality.getValue() && a.value.equals(b.value))
			out.send(1D);
		else out.send(0D);
	}
	
}
