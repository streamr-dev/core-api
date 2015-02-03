package com.unifina.signalpath;

import com.unifina.push.IHasPushChannel;
import com.unifina.utils.IdGenerator;

public abstract class ModuleWithUI extends AbstractSignalPathModule implements IHasPushChannel {

	protected String uiChannelId;
	
	public ModuleWithUI() {
		super();
		uiChannelId = IdGenerator.get();
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
