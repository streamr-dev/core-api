package com.unifina.signalpath.charts;

import com.unifina.signalpath.ModuleOption;
import com.unifina.signalpath.ModuleOptions;

import java.util.Map;

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
	protected MapModuleType getMapModuleType() {
		return new MapModuleType("longitude", "latitude", MapModuleType.XYOrder.YX);
	}

	@Override
	protected Double xToLongitude(Double x) {
		return x; // map accepts longitude as-is
	}

	@Override
	protected Double yToLatitude(Double y) {
		return y; // map accepts latitude as-is
	}

	@Override
	public Map<String, Object> getConfiguration() {
		Map<String, Object> config = super.getConfiguration();

		ModuleOptions options = ModuleOptions.get(config);
		options.addIfMissing(ModuleOption.createString("skin", skin)
			.addPossibleValue("Default", "default")
			.addPossibleValue("Dark", "cartoDark")
		);

		return config;
	}

	@Override
	protected void onConfiguration(Map<String, Object> config) {
		super.onConfiguration(config);
		ModuleOptions options = ModuleOptions.get(config);

		if (options.containsKey("skin")) {
			skin = options.getOption("skin").getString();
		}
	}
}
