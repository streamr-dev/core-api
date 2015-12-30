package com.unifina.signalpath.input;


import com.unifina.signalpath.*;

import java.util.HashMap;
import java.util.Map;


public class ButtonModule extends InputModule {

	StringParameter name = new StringParameter(this, "buttonName", "button");

	DoubleParameter value = new DoubleParameter(this, "outputValue", 0d);

	TimeSeriesOutput out = new TimeSeriesOutput(this, "output");

	String buttonName = null;
	Double buttonValue = null;


	@Override
	public void initialize() {
		if (globals.getUiChannel()!=null) {
			Map<String,Object> buttonMsg = new HashMap<>();
			buttonMsg.put("buttonName", name.getValue());
			buttonName = name.getValue();
			buttonMsg.put("value", value.getValue());
			buttonValue = value.getValue();
			globals.getUiChannel().push(buttonMsg, uiChannelId);
		}
	}

	@Override
	public Map<String, Object> getConfiguration() {
		Map<String, Object> config = super.getConfiguration();
		config.put("module", "StreamrButton");
		return config;
	}

	@Override
	protected void onInput(RuntimeRequest request, RuntimeResponse response) {

	}

	@Override
	public void sendOutput() {
		out.send(buttonValue);
	}

	@Override
	public void trySendOutput() {
		super.trySendOutput();

		boolean nameHasChanged = !name.getValue().equals(buttonName);
		boolean valueHasChanged = value.getValue() != buttonValue;

		if(nameHasChanged || valueHasChanged) {
			Map<String,Object> buttonMsg = new HashMap<>();
			if(nameHasChanged) {
				buttonMsg.put("buttonName", name.getValue());
			}
			if(valueHasChanged) {
				buttonMsg.put("value", value.getValue());
			}
			globals.getUiChannel().push(buttonMsg, uiChannelId);
		}
	}

	@Override
	public void clearState() {
		buttonName = null;
		buttonValue = null;
	}
}
