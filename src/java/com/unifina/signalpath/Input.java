package com.unifina.signalpath;

import com.unifina.security.permission.ConnectionTraversalPermission;

import java.security.AccessController;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class Input<T> extends Endpoint<T> {

	public T value;
	
	private boolean ready = false;
	private boolean wasReady = false;
	
	private Output<T> source;
	
	/**
	 * Input parameters
	 */
	private boolean requiresConnection = true;
	private boolean drivingInput = true;
	private boolean canToggleDrivingInput = true;

	private List<Input<T>> proxiedInputs = new ArrayList<>();
	
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
			getOwner().getDrivingInputs().add(this);
			getOwner().setSendPending(true);
		}

		for (Input<T> p : proxiedInputs) {
			p.receive(value);
		}
	}

	// TODO: should be private, but hacked public for Merge
	public void setReady(boolean ready) {
		if (ready) {
			this.ready = true;
			wasReady = true;
			getOwner().markReady(this);
		} else {
			this.ready = false;
			getOwner().cancelReady(this);
		}
	}

	@Override
	public T getValue() {
		if (value == null) {
			pullValueFromPullableIfConnected();
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

	/**
	 * Pull value from owner if owner implements Pullable
	 * @return true if connected to Pullable
	 */
	protected boolean pullValueFromPullableIfConnected() {
		if (isConnectedToPullable()) {
			Object pulledObject = ((Pullable<?>) source.getOwner()).pullValue(source);
			value = handlePulledObject(pulledObject);
			return true;
		}
		return false;
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

		if (config.containsKey("drivingInput")) {
			drivingInput = Boolean.parseBoolean(config.get("drivingInput").toString());
		}
	}
	
	/**
	 * Attaches an input that will be proxied by this input, ie.
	 * this input acts as an interface masking a list of proxied inputs
	 * that will receive values and participate in dirty marking exactly
	 * like this input.
	 * @param input
	 */
	public void addProxiedInput(Input<T> input) {
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

	public Output<T> getSource() {
		if (System.getSecurityManager() != null) {
			AccessController.checkPermission(new ConnectionTraversalPermission());
		}
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
		if (!isConnectedToPullable()) {
			value = null;
			setReady(false);
		}
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

	public boolean isDrivingInput() {
		return drivingInput;
	}

	public void setDrivingInput(boolean drivingInput) {
		this.drivingInput = drivingInput;
	}

	public boolean isCanToggleDrivingInput() {
		return canToggleDrivingInput;
	}

	public void setCanToggleDrivingInput(boolean canToggleDrivingInput) {
		this.canToggleDrivingInput = canToggleDrivingInput;
	}

	public boolean isRequiresConnection() {
		return requiresConnection;
	}

	public void setRequiresConnection(boolean requiresConnection) {
		this.requiresConnection = requiresConnection;
	}

	@Override
	public String toString() {
		return "(in) " + super.toString() + ", value: " + value;
	}

	private boolean isConnectedToPullable() {
		return isConnected() && source.getOwner() instanceof Pullable;
	}
}
