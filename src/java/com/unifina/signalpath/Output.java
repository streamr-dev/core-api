package com.unifina.signalpath;

import java.util.ArrayList;
import java.util.List;


public class Output<T> extends Endpoint<T> {
	
	private ArrayList<Input<T>> targets = new ArrayList<>();
	// For super fast looping use an array instead of a List Iterator
	private Input<T>[] cachedTargets = new Input[0];
	
	// Used to flag the Propagators that contain this Output that a value has been sent
	private transient ArrayList<Propagator> propagators;
	// For super fast looping use an array instead of a List Iterator
	private transient Propagator[] cachedPropagators;
	
	protected T previousValue = null;
	
	private boolean connected = false;
	private int i;

	private List<Output<T>> proxiedOutputs = new ArrayList<>();
	
	public Output(AbstractSignalPathModule owner,String name,String typeName) {
		super(owner,name,typeName);
	}

	public Input<T>[] getTargets() {
		return cachedTargets;
	}
	
	public void send(T value) {
		if (value == null) {
			throw new NullPointerException("Sending a null value is not allowed!");
		}

		previousValue = value;
		
		if (connected) {

			if (cachedPropagators == null) {
				updateCachedPropagators();
			}

			for (i=0; i < cachedPropagators.length; i++) {
				cachedPropagators[i].sendPending = true;
			}

			for (i=0; i < cachedTargets.length; i++) {
				cachedTargets[i].receive(value);
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

	private void updateCachedPropagators() {
		cachedPropagators = (propagators == null ? new Propagator[0] : propagators.toArray(new Propagator[propagators.size()]));
	}

	public void addPropagator(Propagator p) {
		if (propagators == null) { // after de-serialization
			propagators = new ArrayList<>();
		}
		if (!propagators.contains(p)) {
			propagators.add(p);
			updateCachedPropagators();
		}
	}
	
	public void removePropagator(Propagator p) {
		if (propagators != null && propagators.contains(p)) {
			propagators.remove(p);
			updateCachedPropagators();
		}
	}
	
	public void connect(Input<T> input) {
		targets.add(input);
		input.setSource(this);
		connected = true;
		cachedTargets = targets.toArray(new Input[targets.size()]);
	}

	public void disconnect() {
		for (Input input : targets) {
			input.disconnect();
		}
		targets.clear();
		connected = false;
		cachedTargets = targets.toArray(new Input[targets.size()]);
	}

	public void addProxiedOutput(Output<T> output) {
		proxiedOutputs.add(output);
	}

	@Override
	public String toString() {
		return "(out) "+super.toString()+": "+previousValue;
	}
	
	public boolean isConnected() {
		return connected;
	}

	@Override
	public T getValue() {
		return previousValue;
	}
	
}
