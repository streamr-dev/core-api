package com.unifina.signalpath.foreach;

import com.unifina.signalpath.Input;
import com.unifina.signalpath.Propagator;
import com.unifina.signalpath.SignalPath;

import java.io.Serializable;

public class SubSignalPath implements Serializable {
	private final SignalPath signalPath;
	private final Propagator propagator;

	public SubSignalPath(SignalPath signalPath, Propagator propagator) {
		this.signalPath = signalPath;
		this.propagator = propagator;
	}

	public void feedInput(String inputName, Object value) {
		signalPath.getInput(inputName).receive(value);
	}

	public void propagate() {
		propagator.propagate();
	}

	public Object readOutput(String outputName) {
		return signalPath.getOutput(outputName).getValue();
	}

	void connectTo(String outputName, Input input) {
		signalPath.getOutput(outputName).connect(input);
	}
}
