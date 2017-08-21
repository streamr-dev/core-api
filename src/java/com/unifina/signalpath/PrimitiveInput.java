package com.unifina.signalpath;

import java.util.Map;

public abstract class PrimitiveInput<T> extends Input<T> {

	private boolean canHaveInitialValue = true;
	private T initialValue = null;

	public PrimitiveInput(AbstractSignalPathModule owner, String name, String type) {
		super(owner, name, type);
	}

	@Override
	public void clear() {
		if (initialValue != null) {
			value = initialValue;
		} else {
			super.clear();
		}
	}

	public T getInitialValue() {
		return initialValue;
	}

	public void setInitialValue(T initialValue) {
		this.initialValue = initialValue;

		if (initialValue!=null) {
			boolean wasPending = getOwner().isSendPending();
			this.receive(initialValue);
			// Initial value must not change send pending state
			getOwner().setSendPending(wasPending);
		}
	}

	@Override
	public Map<String,Object> getConfiguration() {
		Map<String,Object> config = super.getConfiguration();

		config.put("canHaveInitialValue", canHaveInitialValue);

		if (canHaveInitialValue && (initialValue == null || validateInitialValue(initialValue))) {
			config.put("initialValue", initialValue);
		}

		return config;
	}

	@Override
	public void setConfiguration(Map<String,Object> config) {
		super.setConfiguration(config);

		if (config.containsKey("initialValue")) {
			setInitialValue(config.get("initialValue") == null || config.get("initialValue").toString().equals("null") ? null : parseInitialValue(config.get("initialValue").toString()));
		}
	}

	protected abstract T parseInitialValue(String initialValue);

	protected boolean validateInitialValue(T initialValue) {
		return true;
	}

	public boolean isCanHaveInitialValue() {
		return canHaveInitialValue;
	}

	public void setCanHaveInitialValue(boolean canHaveInitialValue) {
		this.canHaveInitialValue = canHaveInitialValue;
	}
}
