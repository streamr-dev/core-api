package com.unifina.signalpath.utils;

import java.util.HashMap;
import java.util.Map;

import com.unifina.signalpath.Input;
import com.unifina.signalpath.ModuleWithUI;

public class Label extends ModuleWithUI {

	Input<Object> label = new Input<>(this, "label", "Object");
	
	@Override
	public void init() {
		addInput(label);
		label.setDrivingInput(true);
		label.canToggleDrivingInput = false;
		
		canClearState = false;

		resendAll = false;
		resendLast = 1;
	}

	@Override
	public void sendOutput() {
		if (getGlobals().getUiChannel()!=null) {
			Map<String,Object> msg = new HashMap<>();
			msg.put("value", label.getValue().toString());
			getGlobals().getUiChannel().push(msg, uiChannelId);
		}
	}

	@Override
	public void clearState() {

	}
	
	@Override
	public Map<String, Object> getConfiguration() {
		// Remove default configuration, always force resendLast=1
		Map<String,Object> config = super.getConfiguration();
		config.remove("options");
		return config;
	}
	
	@Override
	public String getUiChannelName() {
		if (label.isConnected()) {
			return super.getUiChannelName() + " ("+label.getSource().getLongName()+")";
		}
		else return super.getUiChannelName();
	}
	
	@Override
	public String getWebcomponentName() {
		return "streamr-label";
	}
	
}
