package com.unifina.signalpath.utils;

import java.util.HashMap;
import java.util.Map;

import com.unifina.signalpath.Input;
import com.unifina.signalpath.ModuleWithUI;
import com.unifina.signalpath.StringParameter;

public class Label extends ModuleWithUI {

	Input<Object> label = new Input<>(this, "label", "Object");
	
	@Override
	public void init() {
		addInput(label);
		label.setDrivingInput(true);
		label.canToggleDrivingInput = false;
		
		canClearState = false;
	}

	@Override
	public void sendOutput() {
		if (globals.getUiChannel()!=null) {
			Map<String,Object> msg = new HashMap<>();
			msg.put("value", label.getValue().toString());
			globals.getUiChannel().push(msg, uiChannelId);
		}
	}

	@Override
	public void clearState() {

	}
	
	@Override
	public Map<String, Object> getConfiguration() {
		Map<String,Object> config = super.getConfiguration();
		addOption(config,"style","string",style);
		return config;
	}

	@Override
	protected void onConfiguration(Map<String, Object> config) {
		super.onConfiguration(config);
		if (getOption(config,"style")!=null)
			style = getOption(config,"style").toString();
	}
	
	@Override
	public String getUiChannelName() {
		if (label.isConnected()) {
			return super.getUiChannelName() + " ("+label.getSource().getLongName()+")";
		}
		else return super.getUiChannelName();
	}
	
}
