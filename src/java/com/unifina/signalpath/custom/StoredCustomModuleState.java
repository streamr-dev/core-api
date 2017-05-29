package com.unifina.signalpath.custom;

import com.unifina.signalpath.Input;
import com.unifina.signalpath.Output;
import com.unifina.signalpath.SignalPath;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

class StoredCustomModuleState implements Serializable {
	private final SignalPath parentSignalPath;
	private final ArrayList<Input> inputs;
	private final Map inputsByName;
	private final ArrayList<Output> outputs;
	private final Map outputsByName;
	private final HashSet<Input> drivingInputs;
	private final Set<Input> readyInputs;

	StoredCustomModuleState(SignalPath parentSignalPath,
							ArrayList<Input> inputs,
							Map inputsByName,
							ArrayList<Output> outputs,
							Map outputsByName,
							HashSet<Input> drivingInputs,
							Set<Input> readyInputs) {
		this.parentSignalPath = parentSignalPath;
		this.inputs = inputs;
		this.inputsByName = inputsByName;
		this.outputs = outputs;
		this.outputsByName = outputsByName;
		this.drivingInputs = drivingInputs;
		this.readyInputs = readyInputs;
	}

	SignalPath getParentSignalPath() {
		return parentSignalPath;
	}

	ArrayList<Input> getInputs() {
		return inputs;
	}

	Map getInputsByName() {
		return inputsByName;
	}

	ArrayList<Output> getOutputs() {
		return outputs;
	}

	Map getOutputsByName() {
		return outputsByName;
	}

	HashSet<Input> getDrivingInputs() {
		return drivingInputs;
	}

	Set<Input> getReadyInputs() {
		return readyInputs;
	}
}
