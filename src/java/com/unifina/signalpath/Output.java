package com.unifina.signalpath;

import java.util.*;


public class Output<T> extends Endpoint<T> {
	
	private ArrayList<Input<T>> targets = new ArrayList<>();
	// For super fast looping use an array instead of a List Iterator
	private Input<T>[] cachedTargets = new Input[0];
	
	// Used to flag the Propagators that contain this Output that a value has been sent
	private ArrayList<Propagator> propagators = new ArrayList<>();
	// For super fast looping use an array instead of a List Iterator
	private Propagator[] cachedPropagators = new Propagator[0];
	
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

		// prevent modification of sent Maps (no copying, just overriding modifying methods)
		//   if a module wants to modify the value, it must make a personal copy
		// see: MapInput.getModifiableValue
		// if T == ConcurrentMap, this breaks
		//   for now, we only have T \in {Object, Map}
		//   if in future we need e.g. ConcurrentMapInput/Output, this needs to be changed, too
		if (value instanceof SortedMap) {
			value = (T)Collections.unmodifiableSortedMap((SortedMap)value);
		} else if (value instanceof Map) {
			value = (T)Collections.unmodifiableMap((Map)value);
		}

		previousValue = value;
		
		if (connected) {
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
	
	public void doClear() {
		previousValue = null;
	}
	
	public void addPropagator(Propagator p) {
		if (!propagators.contains(p)) {
			propagators.add(p);
			cachedPropagators = propagators.toArray(new Propagator[propagators.size()]);
		}
	}
	
	public void removePropagator(Propagator p) {
		if (propagators.contains(p)) {
			propagators.remove(p);
			cachedPropagators = propagators.toArray(new Propagator[propagators.size()]);
		}
	}
	
	public void connect(Input<T> input) {
		targets.add(input);
		input.setSource(this);
		connected = true;
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
	
	public T getValue() {
		return previousValue;
	}
	
}
