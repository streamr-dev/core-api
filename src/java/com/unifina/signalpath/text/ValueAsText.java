package com.unifina.signalpath.text;

import com.unifina.signalpath.AbstractSignalPathModule;
import com.unifina.signalpath.Input;
import com.unifina.signalpath.StringOutput;

public class ValueAsText extends AbstractSignalPathModule {

	Input<Object> in = new Input<>(this,"in","Object");

	StringOutput out = new StringOutput(this,"text");
	
	
	@Override
	public void init() {
		addInput(in);
		addOutput(out);
	}

	@Override
	public void sendOutput() {
		out.send(in.getValue().toString());
	}

	@Override
	public void clearState() {

	}
	
}
