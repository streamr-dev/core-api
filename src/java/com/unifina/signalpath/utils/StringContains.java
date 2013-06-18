package com.unifina.signalpath.utils;

import com.unifina.signalpath.AbstractSignalPathModule;
import com.unifina.signalpath.StringInput;
import com.unifina.signalpath.StringParameter;
import com.unifina.signalpath.TimeSeriesOutput;

public class StringContains extends AbstractSignalPathModule {

	StringParameter s = new StringParameter(this,"search","");
	StringInput in = new StringInput(this,"string");
	TimeSeriesOutput out = new TimeSeriesOutput(this,"contains");
	
	@Override
	public void init() {
		addInput(s);
		addInput(in);
		addOutput(out);
	}

	@Override
	public void sendOutput() {
		// Output nothing if searching for an empty string
		if (!s.getValue().isEmpty()) {
			if (in.getValue().contains(s.getValue()))
				out.send(1);
			else out.send(0);
		}
	}

	@Override
	public void clearState() {

	}

}
