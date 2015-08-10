package com.unifina.signalpath.text;

import com.unifina.signalpath.AbstractSignalPathModule;
import com.unifina.signalpath.StringInput;
import com.unifina.signalpath.TimeSeriesOutput;

public class StringLength extends AbstractSignalPathModule {

	StringInput in = new StringInput(this,"text");

	TimeSeriesOutput out = new TimeSeriesOutput(this,"length");
		
	@Override
	public void init() {
		addInput(in);
		addOutput(out);
	}

	@Override
	public void sendOutput() {
		out.send(in.getValue().length());
	}

	@Override
	public void clearState() {

	}
	
}
