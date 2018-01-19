package com.unifina.signalpath.bool;

import com.unifina.signalpath.AbstractSignalPathModule;
import com.unifina.signalpath.BooleanInput;
import com.unifina.signalpath.BooleanOutput;

public class Xor extends AbstractSignalPathModule {

	private final BooleanInput a = new BooleanInput(this,"A");
	private final BooleanInput b = new BooleanInput(this,"B");

	private final BooleanOutput out = new BooleanOutput(this,"out");

	@Override
	public void init() {
		addInput(a);
		addInput(b);
		addOutput(out);
	}

	@Override
	public void sendOutput() {
		out.send(a.getValue() ^ b.getValue());
	}

	@Override
	public void clearState() {}
}
