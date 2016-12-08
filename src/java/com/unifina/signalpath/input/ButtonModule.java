package com.unifina.signalpath.input;


import com.unifina.signalpath.*;

import java.util.HashMap;
import java.util.Map;


public class ButtonModule extends InputModule {

	StringParameter buttonName = new StringParameter(this, "buttonName", "button");

	DoubleParameter buttonValue = new DoubleParameter(this, "buttonValue", 0d);

	TimeSeriesOutput out = new TimeSeriesOutput(this, "out");


	@Override
	public void init() {
		super.init();
		canClearState = false;

		buttonName.setDrivingInput(true);

		out.noRepeat = false;
		out.canBeNoRepeat = false;

	}

	@Override
	protected void onInput(RuntimeRequest request, RuntimeResponse response) {}

	@Override
	protected void handleRequest(RuntimeRequest request, RuntimeResponse response) {
		super.handleRequest(request, response);
		if (request.getType().equals("getState")) {
			response.put("state", buttonName.getValue());
			response.setSuccess(true);
		}
	}

	@Override
	public void sendOutput() {
		if (drivingInputs.contains(buttonName)) {
			if (getGlobals().getUiChannel()!=null) {
				Map<String,Object> msg = new HashMap<String,Object>();
				msg.put("buttonName", buttonName.getValue());
				getGlobals().getUiChannel().push(msg, uiChannelId);
			}
		}
		if (uiEventSendPending) {
			out.send(buttonValue.getValue());
		}
	}

	@Override
	public void clearState() {}


	@Override
	protected String getWidgetName() {
		return "StreamrButton";
	}
}
