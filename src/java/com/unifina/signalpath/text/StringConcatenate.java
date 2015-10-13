package com.unifina.signalpath.text;

import com.unifina.signalpath.AbstractSignalPathModule;
import com.unifina.signalpath.StringInput;
import com.unifina.signalpath.StringOutput;

public class StringConcatenate extends AbstractSignalPathModule {

	StringInput inA = new StringInput(this,"A");
	StringInput inB = new StringInput(this,"B");
	StringOutput out = new StringOutput(this,"AB");
	
	@Override
	public void init() {
		addInput(inA);
		addInput(inB);
		addOutput(out);
	}

	@Override
	public void sendOutput() {
		out.send(inA.getValue()+inB.getValue());
	}

	@Override
	public void clearState() {

	}

}
