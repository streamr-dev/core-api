package com.unifina.signalpath.text;

import com.unifina.signalpath.AbstractSignalPathModule;
import com.unifina.signalpath.Input;
import com.unifina.signalpath.StringInput;
import com.unifina.signalpath.StringOutput;
import com.unifina.signalpath.variadic.InputInstantiator;
import com.unifina.signalpath.variadic.VariadicInput;

import java.util.Map;

public class StringConcatenate extends AbstractSignalPathModule {

	StringInput inA = new StringInput(this,"A");
	StringInput inB = new StringInput(this,"B");
	VariadicInput<String> inX = new VariadicInput<>(this, new InputInstantiator.Strings(), 3);
	StringOutput out = new StringOutput(this,"AB");
	
	@Override
	public void init() {
		addInput(inA);
		addInput(inB);
		addOutput(out);
	}

	@Override
	public void sendOutput() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(inA.getValue());
		buffer.append(inB.getValue());
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
