package com.unifina.signalpath.charts;

import com.unifina.signalpath.*;
import com.unifina.utils.Color;

import java.util.LinkedHashMap;

public class Map extends ModuleWithUI {

	Input<Object> id = new Input<>(this, "id", "Double String");
	TimeSeriesInput latitude = new TimeSeriesInput(this, "latitude");
	TimeSeriesInput longitude = new TimeSeriesInput(this, "longitude");
	Input<Color> color = new Input<>(this, "color", "Color");

	@Override
	public void init() {
		addInput(id);
		addInput(latitude);
		addInput(longitude);
		addInput(color);
		this.canClearState = false;
		this.resendAll = false;
		latitude.setDrivingInput(true);
		latitude.canToggleDrivingInput = false;
		latitude.canHaveInitialValue = false;
		latitude.canBeFeedback = false;
		longitude.setDrivingInput(true);
		longitude.canToggleDrivingInput = false;
		longitude.canHaveInitialValue = false;
		longitude.canBeFeedback = false;
		color.setDrivingInput(true);
		color.canToggleDrivingInput = false;
		color.canBeFeedback = false;
		id.setDrivingInput(true);
		id.canToggleDrivingInput = false;
		id.canBeFeedback = false;
	}
	
	@Override
	public void sendOutput() {
		if (globals.getUiChannel()!=null) {
			globals.getUiChannel().push(new MapPoint(id.getValue(), latitude.getValue(), longitude.getValue(), color.getValue()), uiChannelId);
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

		return config;
	}

	@Override
	protected void onConfiguration(java.util.Map<String, Object> config) {
		super.onConfiguration(config);
	}

	class MapPoint extends LinkedHashMap<String,Object> {
		public MapPoint(Object id, Double latitude, Double longitude, Color color) {
			super();
			if (!(id instanceof Double || id instanceof String)) {
				throw new RuntimeException("Id must be Double or String!");
			}
			put("t", "p");
			put("lat", latitude);
			put("lng", longitude);
			put("id", id);
			put("color", color.toString());
		}
	}
}
