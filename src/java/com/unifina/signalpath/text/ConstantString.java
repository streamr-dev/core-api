package com.unifina.signalpath.text;

import com.unifina.signalpath.AbstractSignalPathModule;
import com.unifina.signalpath.Input;
import com.unifina.signalpath.Output;
import com.unifina.signalpath.Pullable;
import com.unifina.signalpath.StringOutput;
import com.unifina.signalpath.StringParameter;

public class ConstantString extends AbstractSignalPathModule implements Pullable<String> {

	StringParameter constant = new StringParameter(this,"str","STR");
	StringOutput out = new StringOutput(this,"out");
	
	public ConstantString() {
		super();
//		originatingModule = true;
		initPriority = 40;
	}
	
	@Override
	public void init() {
		addInput(constant);
		addOutput(out);
	}
	
	@Override
	public void initialize() {
//		out.send(constant.getValue());
//		basicPropagator.propagate();
		for (Input i : out.getTargets())
			i.receive(constant.getValue());
	}
	
	@Override
	public void sendOutput() {
		out.send(constant.getValue());
	}

	@Override
	public void clearState() {

	}

	@Override
	public String pullValue(Output output) {
		return constant.getValue();
	}
	
}
