package com.unifina.signalpath.charts;

import com.unifina.signalpath.ModuleOption;
import com.unifina.signalpath.ModuleOptions;
import com.unifina.signalpath.ModuleWithUI;
import com.unifina.signalpath.TimeSeriesInput;

import java.util.LinkedHashMap;

public class Map extends ModuleWithUI {

	TimeSeriesInput id = new TimeSeriesInput(this, "id");
	TimeSeriesInput latitude = new TimeSeriesInput(this, "latitude");
	TimeSeriesInput longitude = new TimeSeriesInput(this, "longitude");

	@Override
	public void init() {
		super.init();
		this.canClearState = false;
		latitude.setDrivingInput(true);
		latitude.canToggleDrivingInput = false;
		latitude.canHaveInitialValue = false;
		latitude.canBeFeedback = false;
		longitude.setDrivingInput(true);
		longitude.canToggleDrivingInput = false;
		longitude.canHaveInitialValue = false;
		longitude.canBeFeedback = false;
		id.setDrivingInput(true);
		id.canToggleDrivingInput = false;
		id.canBeFeedback = false;
	}
	
	@Override
	public void sendOutput() {
		if (globals.getUiChannel()!=null) {
			globals.getUiChannel().push(new MapPoint(latitude.getValue(), longitude.getValue(), id.getValue()), uiChannelId);
		}
	}

	@Override
	public void clearState() {

	}

	@Override
	public String getWebcomponentName() {
		return "streamr-map";
	}

	@Override
	public java.util.Map<String, Object> getConfiguration() {
		java.util.Map<String, Object> config = super.getConfiguration();

		ModuleOptions options = ModuleOptions.get(config);
		options.addIfMissing(new ModuleOption("min", 0, ModuleOption.OPTION_DOUBLE));
		options.addIfMissing(new ModuleOption("max", 20, ModuleOption.OPTION_DOUBLE));
		options.addIfMissing(new ModuleOption("centerLat", 35, ModuleOption.OPTION_DOUBLE));
		options.addIfMissing(new ModuleOption("centerLng", 15, ModuleOption.OPTION_DOUBLE));
		options.addIfMissing(new ModuleOption("zoom", 2, ModuleOption.OPTION_INTEGER));
		options.addIfMissing(new ModuleOption("minZoom", 2, ModuleOption.OPTION_INTEGER));
		options.addIfMissing(new ModuleOption("maxZoom", 18, ModuleOption.OPTION_INTEGER));
		options.addIfMissing(new ModuleOption("radius", 30, ModuleOption.OPTION_DOUBLE));
		options.addIfMissing(new ModuleOption("maxOpacity", 0.8, ModuleOption.OPTION_DOUBLE));
		options.addIfMissing(new ModuleOption("scaleRadius", false, ModuleOption.OPTION_BOOLEAN));
		options.addIfMissing(new ModuleOption("useLocalExtrema", false, ModuleOption.OPTION_BOOLEAN));
		options.addIfMissing(new ModuleOption("lifeTime", 7*1000, ModuleOption.OPTION_INTEGER));
		options.addIfMissing(new ModuleOption("fadeInTime", 500, ModuleOption.OPTION_INTEGER));
		options.addIfMissing(new ModuleOption("fadeOutTime", 500, ModuleOption.OPTION_INTEGER));

		return config;
	}

	@Override
	protected void onConfiguration(java.util.Map<String, Object> config) {
		super.onConfiguration(config);
	}

	class MapPoint extends LinkedHashMap<String,Object> {
		public MapPoint(Double id, Double latitude, Double longitude) {
			super();
			put("t", "p");
			put("lat", latitude);
			put("long", longitude);
			put("id", id);
		}
	}
}
