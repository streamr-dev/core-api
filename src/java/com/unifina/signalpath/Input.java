package com.unifina.signalpath;

import java.util.ArrayList;
import java.util.Map;


public class Input<T> extends Endpoint<T> {

	public T value;
	
	boolean ready = false;
	boolean wasReady = false;
	
	public Output<T> source;
	
	/**
	 * Input parameters
	 */
	boolean feedbackConnection = false;
	public boolean canBeFeedback = true;
	public boolean requiresConnection = true;
	
	boolean drivingInput = true;
	public boolean canToggleDrivingInput = true;

	protected boolean proxying = false;
	ArrayList<Input<T>> proxiedInputs = new ArrayList<>();
	
	public Input(AbstractSignalPathModule owner, String name, String typeName) {
		super(owner, name, typeName);
	}

	// The signature could be receive(T value), but then IntegerParameter would be in trouble due to Java generics
	public void receive(Object value) {
		// Value is guaranteed by the Output to be not null, let's not double-check for efficiency

		this.value = (T) value;
		
		if (!ready) {
			ready = true;
			wasReady = true;
			owner.markReady(this);
		}
		
		if (drivingInput) {
			owner.drivingInputs.add(this);
			owner.setSendPending(true);
		}

		if (proxying) {
			for (Input<T> p : proxiedInputs)
				p.receive(value);
		}
	}

	// TODO: horrible hack for DNI project
	public void setReadyHack() {
		ready = true;
		wasReady = true;
		owner.markReady(this);
	}

	@Override
	public T getValue() {
		return value;
	}
	
	public boolean hasValue() {
		return getValue() != null;
	}
	
	@Override
	public Map<String,Object> getConfiguration() {
		Map<String,Object> config = super.getConfiguration();
		
		config.put("drivingInput", drivingInput);
		config.put("canToggleDrivingInput", canToggleDrivingInput);
		config.put("acceptedTypes", getAcceptedTypes());
		config.put("requiresConnection", requiresConnection);

		if (isConnected()) {
			config.put("sourceId", getSource().getId());
		}
		
		return config;
	}
	
	@Override
	public void setConfiguration(Map<String,Object> config) {
		super.setConfiguration(config);
		
		if (config.containsKey("drivingInput"))
			drivingInput = Boolean.parseBoolean(config.get("drivingInput").toString());
	}
	
	/**
	 * Attaches an input that will be proxied by this input, ie.
	 * this input acts as an interface masking a list of proxied inputs
	 * that will receive values and participate in dirty marking exactly
	 * like this input.
	 * @param input
	 */
	public void addProxiedInput(Input<T> input) {
		proxying = true;
		proxiedInputs.add(input);
		
		if (hasValue()) {
			input.receive(getValue());
		}

		AbstractSignalPathModule owner = input.getOwner();
		if (owner != null) {
			if (input.isReady()) {
				owner.markReady(input);
			} else {
				owner.cancelReady(input);
			}
		}

		// TODO: might be necessary to mark owner as originatingmodule and mark it dirty, fix it when generalizing from subclasses
	}

	public boolean isDrivingInput() {
		return drivingInput;
	}

	public void setDrivingInput(boolean drivingInput) {
		this.drivingInput = drivingInput;
	}

	public Output<T> getSource() {
		return source;
	}

	public void setSource(Output<T> source) {
		this.source = source;
		if (!isReady()) {
			owner.cancelReady(this);
		}
	}

	public void disconnect() {
		this.source = null;
		owner.cancelReady(this);
	}

	@Override
	public void clear() {
		value = null;
		ready = false;
	}

	public boolean isFeedbackConnection() {
		return feedbackConnection;
	}

	public void setFeedbackConnection(boolean feedbackConnection) {
		this.feedbackConnection = feedbackConnection;
	}
	
	@Override
	public boolean isConnected() {
		return source!=null;
	}

	public boolean isReady() {
		return ready || (!isConnected() && !requiresConnection);
	}

	public boolean wasReady() {
		return wasReady;
	}
	
	@Override
	public String toString() {
		return "(in) "+super.toString()+", value: "+value+" "+(feedbackConnection ? " (feedback)" : "");
	}
	
	@SuppressWarnings("rawtypes")
	public boolean dependsOn(AbstractSignalPathModule origin) {
		return dependsOn(origin,new ArrayList<Input>());
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public boolean dependsOn(AbstractSignalPathModule origin, ArrayList<Input> visited) {
		if (source==null) return false;
		else if (origin==source.getOwner()) return true;
		else {
			for (Input i : source.getOwner().getInputs()) {
				// Don't get into an infinite loop
				if (visited.contains(i))
					return false;
				
				visited.add(i);
				if (i.dependsOn(origin,visited)) {
					return true;
				}
				visited.remove(i);
			}
			return false;
		}
	}

}
