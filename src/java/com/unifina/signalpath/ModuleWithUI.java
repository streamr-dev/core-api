package com.unifina.signalpath;

import java.util.HashMap;
import java.util.Map;

import com.unifina.push.IHasPushChannel;
import com.unifina.push.PushChannel;
import com.unifina.utils.IdGenerator;
import com.unifina.utils.MapTraversal;

public abstract class ModuleWithUI extends AbstractSignalPathModule implements IHasPushChannel {

	protected String uiChannelId;
	protected boolean resendAll = false;
	protected int resendLast = 0;
	
	public ModuleWithUI() {
		super();
	}

	protected boolean pushToUiChannel(Object data) {
		PushChannel rc = getGlobals().getUiChannel();
		if (rc == null) {
			return false;
		} else {
			rc.push(data, uiChannelId);
			return true;
		}
	}

	@Override
	public void connectionsReady() {
		if (getUiChannelId() == null) {
			throw new NullPointerException("uiChannelId of moduleWithUi " + getName() + " was unexpectedly null");
		}
		if (getGlobals() !=null && getGlobals().getUiChannel()!=null) {
			getGlobals().getUiChannel().addChannel(uiChannelId);
		}
		super.connectionsReady();
	}
	
	@Override
	public String getUiChannelId() {
		return uiChannelId;
	}
	
	@Override
	public String getUiChannelName() {
		return getEffectiveName();
	}

	public Map getUiChannelMap() {
		Map<String, String> uiChannel = new HashMap<>();
		uiChannel.put("id", getUiChannelId());
		uiChannel.put("name", getUiChannelName());
		uiChannel.put("webcomponent", getWebcomponentName());
		return uiChannel;
	}

	/**
	 * Override this method if a webcomponent is available for this module. The
	 * default implementation returns null, which means there is no webcomponent.
	 * @return The name of the webcomponent.
	 */
	public String getWebcomponentName() {
		if (domainObject == null) {
			return null;
		} else {
			return domainObject.getWebcomponent();
		}
	}
	
	@Override
	public Map<String, Object> getConfiguration() {
		Map<String, Object> config = super.getConfiguration();
		Map uiChannel = getUiChannelMap();
		
		if (getWebcomponentName() != null && getGlobals().isRealtime())
			uiChannel.put("webcomponent", getWebcomponentName());
		
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
