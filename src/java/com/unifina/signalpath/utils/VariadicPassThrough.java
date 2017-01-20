package com.unifina.signalpath.utils;

import com.unifina.signalpath.AbstractSignalPathModule;
import com.unifina.signalpath.Input;
import com.unifina.signalpath.Output;
import com.unifina.signalpath.variadic.InputInstantiator;
import com.unifina.signalpath.variadic.OutputInstantiator;
import com.unifina.signalpath.variadic.VariadicInputOutputPair;

import java.util.List;
import java.util.Map;

public class VariadicPassThrough extends AbstractSignalPathModule {

	private VariadicInputOutputPair<Object> inputOutputPairs = new VariadicInputOutputPair<>(this,
		new InputInstantiator.SimpleObject(), new OutputInstantiator.SimpleObject(), 1);

	@Override
	public void initialize() {
		/*// Input source and Output targets must be of same type
		if (input.isConnected() && output.getTargets().length>0) {
			String type = input.getSource().getTypeName();
			
			// TODO: For now, skip the complex situation in which two Object 
			// endpoints are connected, for example two PassThroughs chained
			if (!type.equals("Object")) {
				for (Input i : output.getTargets())
					if (!i.getTypeName().equals("Object") && !type.equals(i.getTypeName()))
						throw new RuntimeException("PassThrough: input is connected to type "+type+", connection to "+(i.getDisplayName()!=null ? i.getDisplayName() : i.getName())+" is of wrong type ("+i.getTypeName()+")!");
			}
		}*/
	}

	@Override
	public void sendOutput() {
		List<Object> values = inputOutputPairs.getInputValues();
		inputOutputPairs.sendValuesToOutputs(values);
	}

	@Override
	public void clearState() {}

	@Override
	protected void onConfiguration(Map<String, Object> config) {
		super.onConfiguration(config);
		inputOutputPairs.onConfiguration(config);
	}

	@Override
	public Input getInput(String name) {
		Input input = super.getInput(name);
		if (input == null) {
			input = inputOutputPairs.addInput(name);
		}
		return input;
	}

	@Override
	public Output getOutput(String name) {
		Output output = super.getOutput(name);
		if (output == null) {
			output = inputOutputPairs.addOutput(name);
		}
		return output;
	}
}
