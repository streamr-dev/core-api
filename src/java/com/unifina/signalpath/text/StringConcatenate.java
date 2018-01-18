package com.unifina.signalpath.text;

import com.unifina.signalpath.AbstractSignalPathModule;
import com.unifina.signalpath.Input;
import com.unifina.signalpath.StringInput;
import com.unifina.signalpath.StringOutput;
import com.unifina.signalpath.variadic.InputInstantiator;
import com.unifina.signalpath.variadic.VariadicInput;

import java.util.Map;

public class StringConcatenate extends AbstractSignalPathModule {

	StringInput in1 = new StringInput(this, "in1");
	StringInput in2 = new StringInput(this, "in2");
	VariadicInput<String> inX = new VariadicInput<>(this, new InputInstantiator.Strings(), 3);
	StringOutput out = new StringOutput(this, "out");
	
	@Override
	public void init() {
		addInput(in1);
		addInput(in2);
		addOutput(out);
	}

	@Override
	public void sendOutput() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(in1.getValue());
		buffer.append(in2.getValue());
		for (String val : inX.getValues()) {
			buffer.append(val);
		}
		out.send(buffer.toString());
	}

	@Override
	public Input getInput(String name) {
		Input input = super.getInput(name);
		if (input == null) {
			input = inX.addEndpoint(name);
		}
		return input;
	}

	@Override
	protected void onConfiguration(Map<String, Object> config) {
		super.onConfiguration(config);
		inX.onConfiguration(config);
	}

	@Override
	public void clearState() {

	}

}
