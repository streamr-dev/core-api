package com.unifina.signalpath.charts;

import com.unifina.signalpath.*;
import com.unifina.utils.StreamrColor;

import java.util.LinkedHashMap;

public class GeographicalMapModule extends MapModule {

	private double centerLat = 35;
	private double centerLng = 35;
	private int zoom = 2;
	private String skin;    // e.g. "default", "cartoDark", "esriDark"
	private int maxZoom;
	private int minZoom;

	@Override
	public String getWebcomponentName() {
		return "streamr-map";
	}

	@Override
	public java.util.Map<String, Object> getConfiguration() {
		java.util.Map<String, Object> config = super.getConfiguration();

		ModuleOptions options = ModuleOptions.get(config);
		options.addIfMissing(new ModuleOption("centerLat", centerLat, ModuleOption.OPTION_DOUBLE));
		options.addIfMissing(new ModuleOption("centerLng", centerLng, ModuleOption.OPTION_DOUBLE));
		options.addIfMissing(new ModuleOption("maxZoom", maxZoom, ModuleOption.OPTION_INTEGER));
		options.addIfMissing(new ModuleOption("minZoom", minZoom, ModuleOption.OPTION_INTEGER));
		options.addIfMissing(new ModuleOption("skin", skin, ModuleOption.OPTION_STRING)
				.addPossibleValue("Default", "default")
				.addPossibleValue("Dark", "cartoDark")
		);
		options.addIfMissing(new ModuleOption("zoom", zoom, ModuleOption.OPTION_INTEGER));

		return config;
	}

	@Override
	protected void onConfiguration(java.util.Map<String, Object> config) {
		super.onConfiguration(config);
		ModuleOptions options = ModuleOptions.get(config);

		if (options.containsKey("centerLat")) {
			centerLat = options.getOption("centerLat").getDouble();
		}

		if (options.containsKey("centerLng")) {
			centerLng = options.getOption("centerLng").getDouble();
		}

		if (options.containsKey("maxZoom")) {
			maxZoom = options.getOption("maxZoom").getInt();
		}

		if (options.containsKey("minZoom")) {
			minZoom = options.getOption("minZoom").getInt();
		}

		if (options.containsKey("skin")) {
			skin = options.getOption("skin").getString();
		}

		if (options.containsKey("zoom")) {
			zoom = options.getOption("zoom").getInt();
		}

	}

	private static class MapPoint extends LinkedHashMap<String, Object> {
		private MapPoint(Object id, Double latitude, Double longitude, StreamrColor color) {
			super();
			if (!(id instanceof Double || id instanceof String)) {
				id = id.toString();
			}
			put("t", "p");    // type: MapPoint
			put("id", id);
			put("lat", latitude);
			put("lng", longitude);
			put("color", color.toString());
		}
	}
}
