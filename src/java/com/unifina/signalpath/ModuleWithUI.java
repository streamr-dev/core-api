package com.unifina.signalpath;

import java.util.UUID;

import com.unifina.push.IHasPushChannel;

public abstract class ModuleWithUI extends AbstractSignalPathModule implements IHasPushChannel {

	protected String uiChannelId;
	
	@Override
	public void initialize() {
		if (globals!=null && globals.getUiChannel()!=null) {
			uiChannelId = UUID.randomUUID().toString();
			globals.getUiChannel().addChannel(uiChannelId);
		}
	}
	
	@Override
	public String getUiChannelId() {
		return uiChannelId;
	}
	
}
