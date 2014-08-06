package com.unifina.atmosphere;

import org.atmosphere.cpr.AtmosphereConfig;
import org.atmosphere.cpr.BroadcasterConfig;
import org.atmosphere.util.SimpleBroadcaster;

// TODO: can be reverted back to SimpleBroadcaster when Atmosphere issue #720 is fixed
// https://github.com/Atmosphere/atmosphere/issues/720

public class MySimpleBroadcaster extends SimpleBroadcaster {

	public MySimpleBroadcaster(String id, AtmosphereConfig config) {
		super(id, config);
	}

	
	@Override
	protected BroadcasterConfig createBroadcasterConfig(AtmosphereConfig config) {
		// Github version line from DefaultBroadcaster: 
//		return new BroadcasterConfig(config.framework().broadcasterFilters, config, getID());
		
		return new BroadcasterConfig(config.framework().broadcasterFilters(), config);
	}

}
