package com.unifina.signalpath.bool;

import com.unifina.signalpath.*;

public class Or extends AbstractSignalPathModule {

	BooleanInput a = new BooleanInput(this,"A");
	BooleanInput b = new BooleanInput(this,"B");

	BooleanOutput out = new BooleanOutput(this,"out");

	@Override
	public void init() {
		addInput(a);
		addInput(b);
		addOutput(out);
	}

	public void clearState() {

	}

	public void sendOutput() {
		out.send(a.getValue() || b.getValue());
	}
	
}
