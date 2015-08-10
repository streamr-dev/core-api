package com.unifina.signalpath.text;

import com.unifina.signalpath.AbstractSignalPathModule;
import com.unifina.signalpath.StringInput;
import com.unifina.signalpath.StringOutput;

public class StringTrim extends AbstractSignalPathModule {

	StringInput in = new StringInput(this,"text");

	StringOutput out = new StringOutput(this,"trimmedText");
	
	
	@Override
	public void init() {
		addInput(in);
		addOutput(out);
	}

	@Override
	public void sendOutput() {
		out.send(in.getValue().trim());
	}

	@Override
	public void clearState() {

	}
	
}
