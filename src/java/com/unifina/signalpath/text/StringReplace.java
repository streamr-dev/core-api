package com.unifina.signalpath.text;

import com.unifina.signalpath.AbstractSignalPathModule;
import com.unifina.signalpath.StringInput;
import com.unifina.signalpath.StringOutput;
import com.unifina.signalpath.StringParameter;

public class StringReplace extends AbstractSignalPathModule {

	StringParameter s = new StringParameter(this,"search","");
	StringParameter r = new StringParameter(this,"replaceWith","");

	StringInput in = new StringInput(this,"text");

	StringOutput out = new StringOutput(this,"out");
		
	@Override
	public void init() {
		addInput(s);
		addInput(r);
		addInput(in);
		addOutput(out);
	}

	@Override
	public void sendOutput() {
		// Must have both s and r parameters
		if(!s.getValue().isEmpty() && !r.getValue().isEmpty()){
			out.send(in.getValue().replaceAll(s.getValue(), r.getValue()));
		}
	}

	@Override
	public void clearState() {

	}
	
}
