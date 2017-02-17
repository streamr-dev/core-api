package com.unifina.signalpath.charts;

import com.unifina.datasource.ITimeListener;
import com.unifina.signalpath.*;
import com.unifina.utils.IdGenerator;
import com.unifina.utils.StreamrColor;

import java.io.Serializable;
import java.util.*;

abstract class MapModule extends ModuleWithUI implements ITimeListener {
	private static final String DEFAULT_MARKER_ICON = "fa fa-4x fa-long-arrow-up";

	private final Input<Object> id = new Input<>(this, "id", "Object");
	private final Input<Object> label = new Input<>(this, "label", "Object");
	private final TimeSeriesInput latitude = new TimeSeriesInput(this, "latitude");
	private final TimeSeriesInput longitude = new TimeSeriesInput(this, "longitude");
	private final TimeSeriesInput heading = new TimeSeriesInput(this, "heading");		// degrees clockwise ("right-handed down")
	private final ColorParameter color = new ColorParameter(this, "traceColor", new StreamrColor(233, 87, 15));

	private double centerLat;
	private double centerLng;
	private int minZoom;
	private int maxZoom;
	private int zoom;
	private boolean autoZoom;
	private boolean drawTrace = false;
	private int traceWidth = 2;
	private boolean customMarkerLabel = false;

	private boolean directionalMarkers = false;
	private String markerIcon = DEFAULT_MARKER_ICON;

	private int expiringTimeOfMarkerInSecs = 0;
	private Set<Marker> expiringMarkers = new LinkedHashSet<>();

	private int expiringTimeOfTraceInSecs = 0;
	private Set<Point> expiringPoints = new LinkedHashSet<>();

	MapModule(double centerLat, double centerLng, int minZoom, int maxZoom, int zoom, boolean autoZoom) {
		this.centerLat = centerLat;
		this.centerLng = centerLng;
		this.minZoom = minZoom;
		this.maxZoom = maxZoom;
		this.zoom = zoom;
		this.autoZoom = autoZoom;
	}

	@Override
	public void init() {
		addInput(id);
		addInput(latitude);
		addInput(longitude);
		this.resendAll = false;
		this.resendLast = 0;
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
		Marker marker = new Marker(
			id.getValue(),
			latitude.getValue(),
			longitude.getValue(),
			color.getValue()
		);

		if (expiringTimeOfMarkerInSecs > 0) {
			marker.setExpirationTime(getGlobals().getTime().getTime() + (expiringTimeOfMarkerInSecs * 1000));
			expiringMarkers.remove(marker);
			expiringMarkers.add(marker);
		}

		if (drawTrace) {
			String tracePointId = IdGenerator.get();
			marker.setTracePointId(tracePointId);
			if (expiringTimeOfTraceInSecs > 0) {
				Point point = new Point(id.getValue().toString());
				point.setTracePointId(tracePointId);
				point.setExpirationTime(getGlobals().getTime().getTime() + (expiringTimeOfTraceInSecs * 1000));
				expiringPoints.remove(point);
				expiringPoints.add(point);
			}
		}
		if (customMarkerLabel) {
			marker.put("label", label.getValue());
		}
		if (directionalMarkers) {
			marker.put("dir", heading.getValue());
		}
		pushToUiChannel(marker);
	}

	@Override
	public void clearState() {
		expiringMarkers.clear();
		expiringPoints.clear();
	}

	@Override
	public java.util.Map<String, Object> getConfiguration() {
		java.util.Map<String, Object> config = super.getConfiguration();

		ModuleOptions options = ModuleOptions.get(config);
		options.addIfMissing(ModuleOption.createDouble("centerLat", centerLat));
		options.addIfMissing(ModuleOption.createDouble("centerLng", centerLng));
		options.addIfMissing(ModuleOption.createInt("minZoom", minZoom));
		options.addIfMissing(ModuleOption.createInt("maxZoom", maxZoom));
		options.addIfMissing(ModuleOption.createInt("zoom", zoom));
		options.addIfMissing(ModuleOption.createBoolean("autoZoom", autoZoom));
		options.addIfMissing(ModuleOption.createBoolean("drawTrace", drawTrace));
		options.addIfMissing(ModuleOption.createInt("traceWidth", traceWidth));
		options.addIfMissing(ModuleOption.createBoolean("markerLabel", customMarkerLabel));
		options.addIfMissing(ModuleOption.createBoolean("directionalMarkers", directionalMarkers));
		options.addIfMissing(ModuleOption.createInt("expiringTimeOfMarkerInSecs", expiringTimeOfMarkerInSecs));
		options.addIfMissing(ModuleOption.createInt("expiringTimeOfTraceInSecs", expiringTimeOfTraceInSecs));
		options.addIfMissing(ModuleOption.createString("markerIcon", markerIcon)
			.addPossibleValue("Default", DEFAULT_MARKER_ICON)
			.addPossibleValue("Long arrow", "fa fa-4x fa-long-arrow-up")
			.addPossibleValue("Short arrow", "fa fa-2x fa-arrow-up")
			.addPossibleValue("Circled arrow", "fa fa-2x fa-arrow-circle-o-up")
			.addPossibleValue("Wedge", "fa fa-3x fa-chevron-up")
			.addPossibleValue("Double wedge", "fa fa-4x fa-angle-double-up")
			.addPossibleValue("Circled wedge", "fa fa-2x fa-chevron-circle-up")
			.addPossibleValue("Triangle", "fa fa-4x fa-caret-up")
			.addPossibleValue("Triangle box", "fa fa-2x fa-caret-square-o-up")
// 			TODO: Implement rotation logic for these markers (default is 45 deg too much)
//			.addPossibleValue("Airplane", "fa fa-4x fa-plane")
//			.addPossibleValue("Rocket", "fa fa-4x fa-rocket")
		);

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

		if (options.containsKey("minZoom")) {
			minZoom = options.getOption("minZoom").getInt();
		}

		if (options.containsKey("maxZoom")) {
			maxZoom = options.getOption("maxZoom").getInt();
		}

		if (options.containsKey("zoom")) {
			zoom = options.getOption("zoom").getInt();
		}

		if (options.containsKey("autoZoom")) {
			autoZoom = options.getOption("autoZoom").getBoolean();
		}

		if (options.containsKey("drawTrace")) {
			drawTrace = options.getOption("drawTrace").getBoolean();
		}

		if (options.containsKey("traceWidth")) {
			traceWidth = options.getOption("traceWidth").getInt();
		}

		if (options.containsKey("markerLabel")) {
			customMarkerLabel = options.getOption("markerLabel").getBoolean();
		}

		if (options.containsKey("directionalMarkers")) {
			directionalMarkers = options.getOption("directionalMarkers").getBoolean();
		}

		if (options.containsKey("expiringTimeOfMarkerInSecs")) {
			expiringTimeOfMarkerInSecs = options.getOption("expiringTimeOfMarkerInSecs").getInt();
		}

		if (options.containsKey("expiringTimeOfTraceInSecs")) {
			expiringTimeOfTraceInSecs = options.getOption("expiringTimeOfTraceInSecs").getInt();
		}

		if (options.containsKey("markerIcon")) {
			markerIcon = options.getOption("markerIcon").getString();
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

	@Override
	public void setTime(Date time) {
		List<String> expiredMapPointIds = new ArrayList<>();
		Map<Object, List<String>> expiredTracePoints = new HashMap<>();

		if (expiringTimeOfMarkerInSecs > 0) {
			Iterator<Marker> iterator = expiringMarkers.iterator();
			Marker marker;

			while (iterator.hasNext() && (marker = iterator.next()).getExpirationTime() <= time.getTime()) {
				iterator.remove();
				expiredMapPointIds.add(marker.getId().toString());
			}
		}
		if (expiringTimeOfTraceInSecs > 0) {
			Iterator<Point> iterator = expiringPoints.iterator();
			Point point;

			while (iterator.hasNext() && (point = iterator.next()).getExpirationTime() <= time.getTime()) {
				iterator.remove();
				if (!expiredTracePoints.containsKey(point.getMarkerId())) {
					expiredTracePoints.put(point.getMarkerId(), new ArrayList<String>());
				}
				expiredTracePoints.get(point.getMarkerId()).add(point.getTracePointId());
			}
		}
		if (!expiredMapPointIds.isEmpty() || !expiredTracePoints.isEmpty()) {
			pushToUiChannel(new ExpirementList(expiredMapPointIds, expiredTracePoints));
		}
	}

	/**
	 * Marker point
	 */
	private static class Marker extends LinkedHashMap<String, Object> {
		private Long expirationTime;

		private Marker(Object id, Double latitude, Double longitude, StreamrColor color) {
			put("t", "p");	// type: MapPoint
			put("id", id.toString());
			put("lat", latitude);
			put("lng", longitude);
			put("color", color.toString());
		}

		Object getId() {
			return get("id");
		}

		Long getExpirationTime() {
			return expirationTime;
		}

		void setExpirationTime(long expirationTime) {
			this.expirationTime = expirationTime;
		}

		void setTracePointId(String id) {
			put("tracePointId", id);
		}

		@Override
		public boolean equals(Object o) {
			return o != null && o instanceof Marker && getId().equals(((Marker) o).getId());
		}

		@Override
		public int hashCode() {
			return get("id").hashCode();
		}
	}

	/**
	 * Trace point
	 */
	private static class Point implements Serializable {
		private Object markerId;
		private long expirationTime;
		private String tracePointId;

		public Point(Object id) {
			this.markerId = id;
		}

		void setTracePointId(String id) {
			tracePointId = id;
		}

		String getTracePointId() {
			return tracePointId;
		}

		void setExpirationTime(long expirationTime) {
			this.expirationTime = expirationTime;
		}

		long getExpirationTime() {
			return expirationTime;
		}

		Object getMarkerId() {
			return markerId;
		}
	}

	private static class ExpirementList extends LinkedHashMap<String, Object> {
		private ExpirementList(List<String> markerIdList, Map<Object, List<String>> pointList) {
			put("t", "d");
			put("markerList", markerIdList);
			put("pointList", pointList);
		}
	}
}
