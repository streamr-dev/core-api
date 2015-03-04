package com.unifina.signalpath;

import java.util.HashMap;
import java.util.Map;

import com.unifina.push.IHasPushChannel;
import com.unifina.utils.IdGenerator;
import com.unifina.utils.MapTraversal;

public abstract class ModuleWithUI extends AbstractSignalPathModule implements IHasPushChannel {

	protected String uiChannelId;
	protected boolean resendAll = true;
	protected int resendLast = 0;
	
	public ModuleWithUI() {
		super();
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
	
	@Override
	public String getUiChannelName() {
		return getName();
	}
	
	@Override
	public Map<String, Object> getConfiguration() {
		Map<String, Object> config = super.getConfiguration();
		Map uiChannel = new HashMap<String,Object>();
		uiChannel.put("id", getUiChannelId());
		uiChannel.put("name", getUiChannelName());
		config.put("uiChannel", uiChannel);
		
		ModuleOptions options = ModuleOptions.get(config);
		options.add(new ModuleOption("uiResendAll", resendAll, "boolean"));
		options.add(new ModuleOption("uiResendLast", resendLast, "int"));
		
		return config;
	}
	
	@Override
	protected void onConfiguration(Map<String, Object> config) {
		super.onConfiguration(config);
		
		uiChannelId = MapTraversal.getString(config, "uiChannel.id");
		if (uiChannelId==null)
			uiChannelId = IdGenerator.get();
		
		ModuleOptions options = ModuleOptions.get(config);
		if (options.getOption("uiResendAll")!=null) {
			resendAll = options.getOption("uiResendAll").getBoolean();
		}
		if (options.getOption("uiResendLast")!=null) {
			resendLast = options.getOption("uiResendLast").getInt();
		}
		
	}
	
}
