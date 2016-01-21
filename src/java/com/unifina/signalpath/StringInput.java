package com.unifina.signalpath;


import java.util.Map;

public class StringInput extends Input<String> {

	public boolean canHaveInitialValue = true;
	public String initialValue = null;

	public StringInput(AbstractSignalPathModule owner, String name) {
		super(owner, name, "String");
	}

	@Override
	protected void doClear() {
		if (initialValue!=null) {
			value = initialValue;
		}
		else super.doClear();
	}

	public String getInitialValue() {
		return initialValue;
	}

	public void setInitialValue(String initialValue) {
		this.initialValue = initialValue;

		if (initialValue!=null) {
			boolean wasPending = owner.isSendPending();
			this.receive(initialValue);
			// Initial value must not change send pending state
			owner.setSendPending(wasPending);
		}
	}

	public Map<String,Object> getConfiguration() {
		Map<String,Object> config = super.getConfiguration();

		config.put("canHaveInitialValue", canHaveInitialValue);

		if (canHaveInitialValue && initialValue!=null && !initialValue.equals(""))
			config.put("initialValue", initialValue);

		return config;
	}

	@Override
	public void setConfiguration(Map<String,Object> config) {
		super.setConfiguration(config);

		if (config.containsKey("initialValue"))
			setInitialValue(config.get("initialValue")==null || config.get("initialValue").equals("") ? null : (String)config.get("initialValue"));
	}
}
