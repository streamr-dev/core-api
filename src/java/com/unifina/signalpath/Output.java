package com.unifina.signalpath;

import com.unifina.security.permission.ConnectionTraversalPermission;

import java.security.AccessController;
import java.util.ArrayList;
import java.util.List;


public class Output<T> extends Endpoint<T> {
	
	private final List<Input<T>> targets = new ArrayList<>();

	// Used to flag the Propagators that contain this Output that a value has been sent
	private transient ArrayList<Propagator> propagators;
	
	private T previousValue = null;

	private final List<Output<T>> proxiedOutputs = new ArrayList<>();
	
	public Output(AbstractSignalPathModule owner, String name, String typeName) {
		super(owner, name, typeName);
	}

	public List<Input<T>> getTargets() {
		if (System.getSecurityManager() != null) {
			AccessController.checkPermission(new ConnectionTraversalPermission());
		}
		return targets;
	}
	
	public void send(T value) {
		if (value == null) {
			throw new NullPointerException("Sending a null value is not allowed!");
		}

		previousValue = value;
		
		if (isConnected()) {
			if (propagators != null) {
				for (Propagator propagator : propagators) {
					propagator.sendPending = true;
				}
			}

			for (Input<T> target : targets) {
				target.receive(value);
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
			propagators = new ArrayList<>();
		}
		if (!propagators.contains(p)) {
			propagators.add(p);
		}
	}
	
	public void connect(Input<T> input) {
		targets.add(input);
		input.setSource(this);
	}

	public void disconnect() {
		for (Input input : targets) {
			input.disconnect();
		}
		targets.clear();
	}

	public void addProxiedOutput(Output<T> output) {
		proxiedOutputs.add(output);
	}

	@Override
	public String toString() {
		return "(out) " + super.toString() + ": " + previousValue;
	}
	
	public boolean isConnected() {
		return !targets.isEmpty();
	}

	@Override
	public T getValue() {
		return previousValue;
	}
}
