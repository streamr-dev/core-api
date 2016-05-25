package com.unifina.signalpath.bool;

import com.unifina.signalpath.*;

public class Not extends AbstractSignalPathModule {

	BooleanInput in = new BooleanInput(this,"in");
	
	BooleanOutput out = new BooleanOutput(this,"out");
	
	@Override
	public void init() {
		addInput(in);
		addOutput(out);
	}
	
	public void clearState() {
		
	}
	
	public void sendOutput() {
		out.send(!in.getValue());
	}
	
}
