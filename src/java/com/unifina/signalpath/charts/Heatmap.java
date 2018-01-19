package com.unifina.signalpath.charts;

import java.util.LinkedHashMap;
import java.util.Map;

import com.unifina.signalpath.ModuleOption;
import com.unifina.signalpath.ModuleOptions;
import com.unifina.signalpath.ModuleWithUI;
import com.unifina.signalpath.TimeSeriesInput;

public class Heatmap extends ModuleWithUI {

	TimeSeriesInput latitude = new TimeSeriesInput(this, "latitude");
	TimeSeriesInput longitude = new TimeSeriesInput(this, "longitude");
	TimeSeriesInput value = new TimeSeriesInput(this, "value");

	@Override
	public void init() {
		super.init();
		this.canClearState = false;
		latitude.setDrivingInput(true);
		latitude.setCanToggleDrivingInput(false);
		latitude.setCanHaveInitialValue(false);
		longitude.setDrivingInput(true);
		longitude.setCanToggleDrivingInput(false);
		longitude.setCanHaveInitialValue(false);
		value.setDrivingInput(true);
		value.setCanToggleDrivingInput(false);
		value.setCanHaveInitialValue(false);

		resendAll = false;
		resendLast = 0;
	}
	
	@Override
	public void sendOutput() {
		pushToUiChannel(new HeatPoint(latitude.getValue(), longitude.getValue(), value.getValue()));
	}

	@Override
	public void clearState() {

	}

	@Override
	public String getWebcomponentName() {
		return "streamr-heatmap";
	}

	@Override
	public Map<String, Object> getConfiguration() {
		Map<String, Object> config = super.getConfiguration();

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
	protected void onConfiguration(Map<String, Object> config) {
		super.onConfiguration(config);
	}

	class HeatPoint extends LinkedHashMap<String,Object> {
		public HeatPoint(Double latitude, Double longitude, Double value) {
			super();
			put("t", "p");
			put("l", latitude);
			put("g", longitude);
			put("v", value);
		}
	}
}
