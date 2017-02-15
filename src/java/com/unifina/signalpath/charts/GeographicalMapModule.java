package com.unifina.signalpath.charts;

import com.unifina.signalpath.*;
import com.unifina.utils.StreamrColor;

import java.util.LinkedHashMap;

public class GeographicalMapModule extends MapModule {
	private String skin;    // e.g. "default", "cartoDark", "esriDark"

	public GeographicalMapModule() {
		super(35, 35, 2, 18, 2, true);
	}

	@Override
	public String getWebcomponentName() {
		return "streamr-map";
	}

	@Override
	public java.util.Map<String, Object> getConfiguration() {
		java.util.Map<String, Object> config = super.getConfiguration();

		ModuleOptions options = ModuleOptions.get(config);
		options.addIfMissing(new ModuleOption("skin", skin, ModuleOption.OPTION_STRING)
				.addPossibleValue("Default", "default")
				.addPossibleValue("Dark", "cartoDark")
		);

		return config;
	}

	@Override
	protected void onConfiguration(java.util.Map<String, Object> config) {
		super.onConfiguration(config);
		ModuleOptions options = ModuleOptions.get(config);

		if (options.containsKey("skin")) {
			skin = options.getOption("skin").getString();
		}
	}
}
