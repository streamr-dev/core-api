package com.unifina.signalpath;

import java.util.ArrayList;


public abstract class Output<T> extends Endpoint<T> {
	
	private ArrayList<Input<T>> targets = new ArrayList<>();
	// For super fast looping use an array instead of a List Iterator
	private Input<T>[] cachedTargets = new Input[0];
	
	// Used to flag the Propagators that contain this Output that a value has been sent
	private ArrayList<Propagator> propagators = new ArrayList<>();
	// For super fast looping use an array instead of a List Iterator
	private Propagator[] cachedPropagators = new Propagator[0];
	
	private boolean connected = false;
	private int i;
	
	public Output(AbstractSignalPathModule owner,String name,String typeName) {
		super(owner,name,typeName);
	}

	public Input<T>[] getTargets() {
		return cachedTargets;
	}
	
	public void send(T value) {
		// TODO: null check can be removed?
		if (value==null)
			throw new NullPointerException("Sending a null value is not allowed!");
		
		if (connected) {
			for (i=0;i<cachedPropagators.length;i++)
				cachedPropagators[i].sendPending = true;
			
			for (i=0;i<cachedTargets.length;i++)
				cachedTargets[i].receive(value);
		}
	}
	
	public void doClear() {
		
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

	@Override
	public String toString() {
		return "(out) "+super.toString();
	}
	
	public boolean isConnected() {
		return connected;
	}
	
}
