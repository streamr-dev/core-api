package com.unifina.signalpath;

import java.util.Map;

public class TimeSeriesInput extends Input<Double> {
	
	public boolean canHaveInitialValue = true;
	public Double initialValue = null;
	public boolean suppressWarnings = false;
	
	public TimeSeriesInput(AbstractSignalPathModule owner, String name) {
		super(owner, name, "Double");
	}
	
	@Override
	protected void doClear() {
		if (initialValue!=null || feedbackConnection) {
			value = initialValue;
		}
		else super.doClear();
	}

	public Double getInitialValue() {
		return initialValue;
	}

	public void setInitialValue(Double initialValue) {
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
		
		if (canHaveInitialValue && initialValue!=null && !initialValue.isNaN() && !initialValue.isInfinite()) 
			config.put("initialValue", initialValue);

		config.put("feedback", isFeedbackConnection());
		config.put("canBeFeedback", canBeFeedback);
		
		if (suppressWarnings)
			config.put("suppressWarnings",suppressWarnings);
		
		return config;
	}

	@Override
	public void setConfiguration(Map<String,Object> config) {
		super.setConfiguration(config);
		
		if (config.containsKey("feedback"))
			setFeedbackConnection(Boolean.parseBoolean(config.get("feedback").toString()));
		
		if (config.containsKey("initialValue"))
			setInitialValue(config.get("initialValue")==null || config.get("initialValue").toString().equals("null") ? null : Double.parseDouble(config.get("initialValue").toString()));
	}
	
}
