package com.unifina.signalpath.input;

import com.unifina.signalpath.*;

import java.util.HashMap;
import java.util.Map;


public class SwitcherModule extends InputModule {

	TimeSeriesOutput out = new TimeSeriesOutput(this, "out");

	boolean value = false;

	@Override
	public void init() {
		super.init();
		canClearState = false;
		resendAll = false;
		resendLast = 1;

		out.canBeNoRepeat = false;
	}

	@Override
	protected void onConfiguration(Map<String, Object> config) {
		super.onConfiguration(config);
		if(config.containsKey("switcherValue"))
			value = (boolean)config.get("switcherValue");
	}

	@Override
	public Map<String, Object> getConfiguration() {
		Map<String, Object> config = super.getConfiguration();
		config.put("switcherValue", value);
		return config;
	}

	@Override
	protected String getWidgetName() {
		return "StreamrSwitcher";
	}

	@Override
	public void initialize() {
		for (Input i : out.getTargets()) {
			if (i instanceof TimeSeriesInput)
				((TimeSeriesInput)i).setInitialValue(value ? 1d : 0d);
			else if (i instanceof IntegerParameter)
				((IntegerParameter) i).receive(value ? 1 : 0);
			else i.receive(value ? 1d : 0d);
		}
	}

	@Override
	protected void onInput(RuntimeRequest request, RuntimeResponse response) {
		value = (boolean) request.get("value");
	}

	@Override
	public void sendOutput() {
		out.send(value ? 1d : 0d);
		if (globals.getUiChannel()!=null) {
			Map<String,Object> msg = new HashMap<String,Object>();
			msg.put("switcherValue", value);
			globals.getUiChannel().push(msg, uiChannelId);
		}
	}

	@Override
	public void clearState() {
		value = false;
	}
}
