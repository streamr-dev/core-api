package com.unifina.signalpath.charts;

import com.unifina.signalpath.*;
import com.unifina.utils.StreamrColor;

import java.util.LinkedHashMap;

public class MapModule extends ModuleWithUI {

	Input<Object> id = new Input<>(this, "id", "Object");
	Input<Object> label = new Input<>(this, "label", "Object");
	TimeSeriesInput latitude = new TimeSeriesInput(this, "latitude");
	TimeSeriesInput longitude = new TimeSeriesInput(this, "longitude");
	ColorParameter color = new ColorParameter(this, "traceColor", new StreamrColor(233, 87, 15));

	boolean drawTrace = false;
	boolean autoZoom = true;
	int traceRadius = 2;
	boolean customMarkerLabel = false;

	@Override
	public void init() {
		addInput(id);
		addInput(latitude);
		addInput(longitude);
		this.canClearState = false;
		this.resendAll = false;
		latitude.setDrivingInput(true);
		latitude.canHaveInitialValue = false;
		latitude.canBeFeedback = false;
		longitude.setDrivingInput(true);
		longitude.canHaveInitialValue = false;
		longitude.canBeFeedback = false;
		id.setDrivingInput(true);
		id.canBeFeedback = false;
		id.requiresConnection = false;
		label.setDrivingInput(false);
		label.canBeFeedback = false;
	}

	@Override
	public void initialize() {
		super.initialize();
		if (!id.isConnected())
			id.receive("DefaultId");
	}

	@Override
	public void sendOutput() {
		MapPoint mapPoint = new MapPoint(
			id.getValue(),
			customMarkerLabel ? label.getValue() : id.getValue(),
			latitude.getValue(),
			longitude.getValue(),
			color.getValue()
		);
		pushToUiChannel(mapPoint);
	}

	@Override
	public void clearState() {}

	@Override
	public String getWebcomponentName() {
		return "streamr-map";
	}

	@Override
	public java.util.Map<String, Object> getConfiguration() {
		java.util.Map<String, Object> config = super.getConfiguration();

		ModuleOptions options = ModuleOptions.get(config);
		options.addIfMissing(new ModuleOption("centerLat", 35, ModuleOption.OPTION_DOUBLE));
		options.addIfMissing(new ModuleOption("centerLng", 15, ModuleOption.OPTION_DOUBLE));
		options.addIfMissing(new ModuleOption("zoom", 2, ModuleOption.OPTION_INTEGER));
		options.addIfMissing(new ModuleOption("minZoom", 2, ModuleOption.OPTION_INTEGER));
		options.addIfMissing(new ModuleOption("maxZoom", 18, ModuleOption.OPTION_INTEGER));
		options.addIfMissing(new ModuleOption("drawTrace", drawTrace, ModuleOption.OPTION_BOOLEAN));
		options.addIfMissing(new ModuleOption("autoZoom", autoZoom, ModuleOption.OPTION_BOOLEAN));
		options.addIfMissing(new ModuleOption("traceRadius", traceRadius, ModuleOption.OPTION_INTEGER));
		options.addIfMissing(new ModuleOption("customMarkerLabel", customMarkerLabel, ModuleOption.OPTION_BOOLEAN));
		options.addIfMissing(new ModuleOption("skin", "default", ModuleOption.OPTION_STRING)
			.addPossibleValue("Default", "default")
			.addPossibleValue("Dark", "cartoDark")
		);

		return config;
	}

	@Override
	protected void onConfiguration(java.util.Map<String, Object> config) {
		super.onConfiguration(config);
		ModuleOptions options = ModuleOptions.get(config);

		if (options.containsKey("drawTrace")) {
			drawTrace = options.getOption("drawTrace").getBoolean();
		}

		if (options.containsKey("autoZoom")) {
			autoZoom = options.getOption("autoZoom").getBoolean();
		}

		if (options.containsKey("traceRadius")) {
			traceRadius = options.getOption("traceRadius").getInt();
		}

		if (options.containsKey("customMarkerLabel")) {
			customMarkerLabel = options.getOption("customMarkerLabel").getBoolean();
		}

		if (drawTrace) {
			addInput(color);
		}

		if (customMarkerLabel) {
			addInput(label);
		}
	}

	private static class MapPoint extends LinkedHashMap<String,Object> {
		private MapPoint(Object id, Object label, Double latitude, Double longitude, StreamrColor color) {
			super();
			if (!(id instanceof Double || id instanceof String)) {
				id = id.toString();
			}
			put("label", label);
			put("t", "p");
			put("lat", latitude);
			put("lng", longitude);
			put("id", id);
			put("color", color.toString());
		}
	}
}
