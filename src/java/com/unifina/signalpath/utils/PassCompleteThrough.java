package com.unifina.signalpath.utils;

import com.unifina.signalpath.AbstractSignalPathModule;
import com.unifina.signalpath.Input;
import com.unifina.signalpath.Output;
import com.unifina.signalpath.variadic.InputInstantiator;
import com.unifina.signalpath.variadic.OutputInstantiator;
import com.unifina.signalpath.variadic.VariadicInputOutputPair;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class PassCompleteThrough extends AbstractSignalPathModule {
	private final VariadicInputOutputPair<Object> inputOutputPairs = new VariadicInputOutputPair<>(this,
		new AlwaysDrivingInputInstantiator(),
		new OutputInstantiator.SimpleObject(),
		1);

	@Override
	public void sendOutput() {
		List<Object> values = inputOutputPairs.getInputValues();
		if (drivingInputs.size() == values.size()) {
			inputOutputPairs.sendValuesToOutputs(values);
		}
	}

	@Override
	public void clearState() {}

	private static class AlwaysDrivingInputInstantiator implements InputInstantiator<Object>, Serializable {
		@Override
		public Input<Object> instantiate(AbstractSignalPathModule owner, String inputName) {
			Input<Object> in = new Input<>(owner, inputName, "Object");
			in.setDrivingInput(true);
			in.canToggleDrivingInput = false;
			return in;
		}
	}

	/**
	 * Boilerplate
	 */
	@Override
	protected void onConfiguration(Map<String, Object> config) {
		super.onConfiguration(config);
		inputOutputPairs.onConfiguration(config);
	}

	/**
	 * Boilerplate
	 */
	@Override
	public Input getInput(String name) {
		Input input = super.getInput(name);
		if (input == null) {
			input = inputOutputPairs.addInput(name);
		}
		return input;
	}

	/**
	 * Boilerplate
	 */
	@Override
	public Output getOutput(String name) {
		Output output = super.getOutput(name);
		if (output == null) {
			output = inputOutputPairs.addOutput(name);
		}
		return output;
	}

}
