package com.unifina.signalpath.text;

import com.unifina.signalpath.AbstractSignalPathModule;
import com.unifina.signalpath.StringInput;
import com.unifina.signalpath.StringOutput;

public class ToLowerCase extends AbstractSignalPathModule {

	StringInput in = new StringInput(this,"text");

	StringOutput out = new StringOutput(this,"lowerCaseText");
	
	
	@Override
	public void init() {
		addInput(in);
		addOutput(out);
	}

	@Override
	public void sendOutput() {
		out.send(in.getValue().toLowerCase());
	}

	@Override
	public void clearState() {

	}
	
}
