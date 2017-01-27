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
			setReady(true);
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

	// TODO: should be private, but hacked public for Merge
	public void setReady(boolean ready) {
		if (ready) {
			this.ready = true;
			wasReady = true;
			owner.markReady(this);
		} else {
			this.ready = false;
			owner.cancelReady(this);
		}
	}

	@Override
	public T getValue() {
		if (value == null && isConnected() && source.getOwner() instanceof Pullable) {
			Object pulledObject = ((Pullable<?>) source.getOwner()).pullValue(source);
			value = handlePulledObject(pulledObject);
		}
		return value;
	}
	
	public boolean hasValue() {
		return getValue() != null;
	}

	/**
	 * If the pulled object is not necessarily an instance of T, this method
	 * should be overridden in a subclass to handle that situation (for example,
	 * an IntegerParameter might pull a Double from Constant).
	 * @param o
	 * @return
	 */
	protected T handlePulledObject(Object o) {
		return (T) o;
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

		if (source.getOwner() instanceof Pullable) {
			setReady(true);
		} else if (!isReady()) {
			setReady(false);
		}
	}

	public void disconnect() {
		if (isConnected()) {
			this.source = null;
			setReady(false);
		}
	}

	@Override
	public void clear() {
		if (!isConnected() || !(source.getOwner() instanceof Pullable)) {
			value = null;
			setReady(false);
		}
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
