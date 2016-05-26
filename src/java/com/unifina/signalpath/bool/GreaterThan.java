package com.unifina.signalpath.bool;

import com.unifina.signalpath.*;

public class GreaterThan extends AbstractSignalPathModule {

	BooleanParameter equality = new BooleanParameter(this,"equality",false);
	
	TimeSeriesInput a = new TimeSeriesInput(this,"A");
	TimeSeriesInput b = new TimeSeriesInput(this,"B");
	
	BooleanOutput out = new BooleanOutput(this,"A&gt;B");
	
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
			out.send(true);
		else out.send(false);
	}
	
}
