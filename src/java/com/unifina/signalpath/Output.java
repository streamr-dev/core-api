package com.unifina.signalpath;

import com.unifina.security.permission.ConnectionTraversalPermission;

import java.security.AccessController;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class Output<T> extends Endpoint<T> {

	// Array for loop efficiency
	private Input<T>[] targets = new Input[0];

	// Used to flag the Propagators that contain this Output that a value has been sent
	// Array for loop efficiency
	private transient Propagator[] propagators;
	
	private T previousValue = null;

	private final List<Output<T>> proxiedOutputs = new ArrayList<>();
	
	public Output(AbstractSignalPathModule owner, String name, String typeName) {
		super(owner, name, typeName);
	}

	public List<Input<T>> getTargets() {
		if (System.getSecurityManager() != null) {
			AccessController.checkPermission(new ConnectionTraversalPermission());
		}
		return Arrays.asList(targets);
	}
	
	public void send(T value) {
		if (value == null) {
			throw new NullPointerException("Sending a null value is not allowed!");
		}

		previousValue = value;
		
		if (isConnected()) {
			if (propagators != null) {
				for (int i=0; i < propagators.length; ++i) {
					propagators[i].sendPending = true;
				}
			}

			for (int i=0; i < targets.length; ++i) {
				targets[i].receive(value);
			}
		}

		for (Output<T> proxy : proxiedOutputs) {
			proxy.send(value);
		}
	}

	@Override
	public void clear() {
		previousValue = null;
	}

	public void addPropagator(Propagator p) {
		if (propagators == null) { // after de-serialization
			propagators = new Propagator[0];
		}

		for (int i=0; i < propagators.length; ++i) {
			if (propagators[i].equals(p)) {
				return;
			}
		}

		propagators = Arrays.copyOf(propagators, propagators.length + 1);
		propagators[propagators.length - 1] = p;
	}
	
	public void connect(Input<T> input) {
		targets = Arrays.copyOf(targets, targets.length + 1);
		targets[targets.length - 1] = input;
		input.setSource(this);
	}

	public void disconnect() {
		for (Input input : targets) {
			input.disconnect();
		}
		targets = new Input[0];
	}

	public void addProxiedOutput(Output<T> output) {
		proxiedOutputs.add(output);
	}

	@Override
	public String toString() {
		return "(out) " + super.toString() + ": " + previousValue;
	}
	
	public boolean isConnected() {
		return targets.length != 0;
	}

	@Override
	public T getValue() {
		return previousValue;
	}
}
