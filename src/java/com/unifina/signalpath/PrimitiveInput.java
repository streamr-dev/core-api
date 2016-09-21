package com.unifina.signalpath;

import java.util.Map;

public abstract class PrimitiveInput<T> extends Input<T> {

	public boolean canHaveInitialValue = true;
	public T initialValue = null;

	public PrimitiveInput(AbstractSignalPathModule owner, String name, String type) {
		super(owner, name, type);
	}

	@Override
	public void clear() {
		if (initialValue!=null || feedbackConnection) {
			value = initialValue;
		}
		else super.clear();
	}

	public T getInitialValue() {
		return initialValue;
	}

	public void setInitialValue(T initialValue) {
		this.initialValue = initialValue;

		if (initialValue!=null) {
			boolean wasPending = owner.isSendPending();
			this.receive(initialValue);
			// Initial value must not change send pending state
			owner.setSendPending(wasPending);
		}
	}

	@Override
	public Map<String,Object> getConfiguration() {
		Map<String,Object> config = super.getConfiguration();

		config.put("canHaveInitialValue", canHaveInitialValue);

		if (canHaveInitialValue && (initialValue==null || validateInitialValue(initialValue)))
			config.put("initialValue", initialValue);

		config.put("feedback", isFeedbackConnection());
		config.put("canBeFeedback", canBeFeedback);

		return config;
	}

	@Override
	public void setConfiguration(Map<String,Object> config) {
		super.setConfiguration(config);

		if (config.containsKey("feedback"))
			setFeedbackConnection(Boolean.parseBoolean(config.get("feedback").toString()));

		if (config.containsKey("initialValue"))
			setInitialValue(config.get("initialValue")==null || config.get("initialValue").toString().equals("null") ? null : parseInitialValue(config.get("initialValue").toString()));
	}

	protected abstract T parseInitialValue(String initialValue);

	protected boolean validateInitialValue(T initialValue) {
		return true;
	}

}
