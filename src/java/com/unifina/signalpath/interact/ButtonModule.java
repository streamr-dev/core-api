package com.unifina.signalpath.interact;


import com.unifina.signalpath.DoubleParameter;
import com.unifina.signalpath.ModuleWithUI;
import com.unifina.signalpath.StringParameter;
import com.unifina.signalpath.TimeSeriesOutput;

import java.util.HashMap;
import java.util.Map;


public class ButtonModule extends ModuleWithUI {

	StringParameter name = new StringParameter(this, "buttonName", "button");

	DoubleParameter value = new DoubleParameter(this, "outputValue", 0d);

	TimeSeriesOutput out = new TimeSeriesOutput(this, "output");

	String buttonName = null;
	Double buttonValue = null;

	@Override
	public void init() {
		addInput(name);
		addInput(value);
		addOutput(out);
	}

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
	public void sendOutput() {

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
