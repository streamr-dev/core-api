package com.unifina.signalpath.charts;

import com.unifina.signalpath.*;
import com.unifina.utils.StreamrColor;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

public class MapModule extends ModuleWithUI {
	public final String DEFAULT_MARKER_ICON = "fa fa-4x fa-long-arrow-up";

	Input<Object> id = new Input<>(this, "id", "Object");
	Input<Object> label = new Input<>(this, "label", "Object");
	TimeSeriesInput latitude = new TimeSeriesInput(this, "latitude");
	TimeSeriesInput longitude = new TimeSeriesInput(this, "longitude");
	TimeSeriesInput heading = new TimeSeriesInput(this, "heading");		// degrees clockwise ("right-handed down")
	ColorParameter color = new ColorParameter(this, "traceColor", new StreamrColor(233, 87, 15));

	boolean drawTrace = false;
	boolean autoZoom = true;
	int traceRadius = 2;
	boolean customMarkerLabel = false;
	String skin;	// e.g. "default", "cartoDark", "esriDark"
	boolean directionalMarkers = false;
	String markerIcon = DEFAULT_MARKER_ICON;

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
		heading.requiresConnection = false;
		heading.canBeFeedback = false;
	}

	@Override
	public void initialize() {
		super.initialize();
		if (!id.isConnected()) {
			id.receive("id");
		}
	}

	@Override
	public void sendOutput() {
		MapPoint mapPoint = new MapPoint(
			id.getValue(),
			latitude.getValue(),
			longitude.getValue(),
			color.getValue()
		);
		if (customMarkerLabel) { mapPoint.put("label", label.getValue()); }
		if (directionalMarkers) { mapPoint.put("dir", heading.getValue()); }
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
		options.addIfMissing(new ModuleOption("markerLabel", customMarkerLabel, ModuleOption.OPTION_BOOLEAN));
		options.addIfMissing(new ModuleOption("directionalMarkers", directionalMarkers, ModuleOption.OPTION_BOOLEAN));
		options.addIfMissing(new ModuleOption("skin", skin, ModuleOption.OPTION_STRING)
			.addPossibleValue("Default", "default")
			.addPossibleValue("Dark", "cartoDark")
		);

		if (directionalMarkers) {
			options.addIfMissing(new ModuleOption("markerIcon", markerIcon, ModuleOption.OPTION_STRING)
				.addPossibleValue("Default", DEFAULT_MARKER_ICON)
				.addPossibleValue("Long arrow", "fa fa-4x fa-long-arrow-up")
				.addPossibleValue("Short arrow", "fa fa-2x fa-arrow-up")
				.addPossibleValue("Circled arrow", "fa fa-2x fa-arrow-circle-o-up")
				.addPossibleValue("Wedge", "fa fa-3x fa-chevron-up")
				.addPossibleValue("Double wedge", "fa fa-4x fa-angle-double-up")
				.addPossibleValue("Circled wedge", "fa fa-2x fa-chevron-circle-up")
				.addPossibleValue("Triangle", "fa fa-4x fa-caret-up")
				.addPossibleValue("Triangle box", "fa fa-2x fa-caret-square-o-up")
				.addPossibleValue("Airplane", "fa fa-4x fa-plane")
				.addPossibleValue("Rocket", "fa fa-4x fa-rocket")
			);
		}

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

		if (options.containsKey("markerLabel")) {
			customMarkerLabel = options.getOption("markerLabel").getBoolean();
		}

		if (options.containsKey("directionalMarkers")) {
			directionalMarkers = options.getOption("directionalMarkers").getBoolean();
		}

		if (drawTrace) {
			addInput(color);
		}

		if (customMarkerLabel) {
			addInput(label);
		}

		if (directionalMarkers) {
			addInput(heading);
		}
	}

	private static class MapPoint extends LinkedHashMap<String,Object> {
		private MapPoint(Object id, Double latitude, Double longitude, StreamrColor color) {
			super();
			if (!(id instanceof Double || id instanceof String)) {
				id = id.toString();
			}
			put("t", "p");	// type: MapPoint
			put("id", id);
			put("lat", latitude);
			put("lng", longitude);
			put("color", color.toString());
		}
	}
}
