package com.unifina.signalpath.text;

import java.util.Arrays;

import com.unifina.signalpath.AbstractSignalPathModule;
import com.unifina.signalpath.ListOutput;
import com.unifina.signalpath.StringInput;
import com.unifina.signalpath.StringParameter;

public class StringSplit extends AbstractSignalPathModule {

	StringParameter s = new StringParameter(this, "separator", "");
	
	StringInput in = new StringInput(this,"text");

	ListOutput out = new ListOutput(this,"list");
		
	@Override
	public void init() {
		addInput(s);
		addInput(in);
		addOutput(out);
	}

	@Override
	public void sendOutput() {
		out.send(Arrays.asList(in.getValue().split(s.getValue())));
	}

	@Override
	public void clearState() {

	}
	
}
