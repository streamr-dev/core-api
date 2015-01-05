package com.unifina.signalpath;

import java.util.UUID;

import com.unifina.push.IHasPushChannel;

public abstract class ModuleWithUI extends AbstractSignalPathModule implements IHasPushChannel {

	protected String uiChannelId;
	
	public ModuleWithUI() {
		super();
		uiChannelId = UUID.randomUUID().toString();
	}
	
	@Override
	public void connectionsReady() {
		if (globals!=null && globals.getUiChannel()!=null) {
			globals.getUiChannel().addChannel(uiChannelId);
		}
		super.connectionsReady();
	}
	
	@Override
	public String getUiChannelId() {
		return uiChannelId;
	}
	
}
