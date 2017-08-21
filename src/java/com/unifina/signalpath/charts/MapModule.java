package com.unifina.signalpath.charts;

import com.unifina.datasource.ITimeListener;
import com.unifina.signalpath.*;
import com.unifina.utils.StreamrColor;

import java.io.Serializable;
import java.util.*;

abstract class MapModule extends ModuleWithUI implements ITimeListener {

	private final Input<Object> id = new Input<>(this, "id", "Object");
	private final Input<Object> label = new Input<>(this, "label", "Object");
	private final TimeSeriesInput xInput = new TimeSeriesInput(this, "x");
	private final TimeSeriesInput yInput = new TimeSeriesInput(this, "y");
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

	private String directionalMarkerIcon = "arrow";
	private String markerIcon = "pin";

	private int expiringTimeOfMarkerInSecs = 0;
	private final Set<ExpiringItem> expiringMarkers = new LinkedHashSet<>();

	private int expiringTimeOfTraceInSecs = 0;
	private final List<ExpiringItem> expiringTracePoints = new LinkedList<>();

	private StreamrColor markerColor = new StreamrColor(233, 91, 21);

	private final MapModuleType type;

	MapModule(double centerLat, double centerLng, int minZoom, int maxZoom, int zoom, boolean autoZoom) {
		this.centerLat = centerLat;
		this.centerLng = centerLng;
		this.minZoom = minZoom;
		this.maxZoom = maxZoom;
		this.zoom = zoom;
		this.autoZoom = autoZoom;
		this.type = getMapModuleType();
	}

	abstract protected MapModuleType getMapModuleType();
	abstract protected Double xToLongitude(Double x);
	abstract protected Double yToLatitude(Double y);

	@Override
	public void init() {
		addInput(id);
		xInput.setName(type.xName);
		yInput.setName(type.yName);
		addInput(type.isXYOrder() ? xInput : yInput);
		addInput(type.isXYOrder() ? yInput : xInput);

		this.resendAll = false;
		this.resendLast = 0;
		yInput.setDrivingInput(true);
		yInput.setCanHaveInitialValue(false);
		xInput.setDrivingInput(true);
		xInput.setCanHaveInitialValue(false);
		id.setDrivingInput(true);
		id.setRequiresConnection(false);
		label.setDrivingInput(false);
		heading.setRequiresConnection(false);
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
			yToLatitude(yInput.getValue()),
			xToLongitude(xInput.getValue()),
			color.getValue()
		);

		if (expiringTimeOfMarkerInSecs > 0) {
			long expireTime = getGlobals().getTime().getTime() + (expiringTimeOfMarkerInSecs * 1000);
			ExpiringItem expiringMarker = new ExpiringItem(id.getValue(), expireTime);
			expiringMarkers.remove(expiringMarker);
			expiringMarkers.add(expiringMarker);
		}

		if (drawTrace) {
			String tracePointId = getGlobals().getIdGenerator().generate();
			marker.put("tracePointId", tracePointId);
			if (expiringTimeOfTraceInSecs > 0) {
				long expireTime = getGlobals().getTime().getTime() + (expiringTimeOfTraceInSecs * 1000);
				expiringTracePoints.add(new ExpiringItem(tracePointId, expireTime));
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
		expiringTracePoints.clear();
	}

	@Override
	public Map<String, Object> getConfiguration() {
		Map<String, Object> config = super.getConfiguration();

		ModuleOptions options = ModuleOptions.get(config);
		// TODO: should rename center options to whatever x/y inputs are named, but this would break js
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
		options.addIfMissing(ModuleOption.createColor("markerColor", markerColor));
		options.addIfMissing(ModuleOption.createString("directionalMarkerIcon", directionalMarkerIcon)
			.addPossibleValue("Arrowhead", "arrowhead")
			.addPossibleValue("Arrow", "arrow")
			.addPossibleValue("Long arrow", "longArrow")
		);
		options.addIfMissing(ModuleOption.createString("markerIcon", markerIcon)
			.addPossibleValue("Pin", "pin")
			.addPossibleValue("Circle", "circle")
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

		if (options.containsKey("directionalMarkerIcon")) {
			directionalMarkerIcon = options.getOption("directionalMarkerIcon").getString();
		}

		if (options.containsKey("markerIcon")) {
			markerIcon = options.getOption("markerIcon").getString();
		}

		if (options.containsKey("markerColor")) {
			markerColor = options.getOption("markerColor").getColor();
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
		List<Object> expiredMapPointIds = new ArrayList<>();
		List<Object> expiredTracePoints = new ArrayList<>();

		if (expiringTimeOfMarkerInSecs > 0) {
			Iterator<ExpiringItem> iterator = expiringMarkers.iterator();
			ExpiringItem marker;

			while (iterator.hasNext() && (marker = iterator.next()).isExpired(time)) {
				iterator.remove();
				expiredMapPointIds.add(marker.getId());
			}
		}
		if (expiringTimeOfTraceInSecs > 0) {
			Iterator<ExpiringItem> iterator = expiringTracePoints.iterator();
			ExpiringItem tracePoint;

			while (iterator.hasNext() && (tracePoint = iterator.next()).isExpired(time)) {
				expiredTracePoints.add(tracePoint.getId());
				iterator.remove();
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
		private Marker(Object id, Double latitude, Double longitude, StreamrColor color) {
			put("t", "p");	// type: MapPoint
			put("id", id);
			put("lat", latitude);
			put("lng", longitude);
			put("color", color.toString());
		}
	}

	private static class ExpiringItem implements Serializable {
		private final Object id;
		private final long expirationTime;

		private ExpiringItem(Object id, long expirationTime) {
			this.id = id;
			this.expirationTime = expirationTime;
		}

		private Object getId() {
			return id;
		}

		private boolean isExpired(Date currentTime) {
			return expirationTime <= currentTime.getTime();
		}

		@Override
		public boolean equals(Object o) {
			return o != null && o instanceof ExpiringItem && getId().equals(((ExpiringItem) o).getId());
		}

		@Override
		public int hashCode() {
			return getId().hashCode();
		}
	}

	private static class ExpirementList extends LinkedHashMap<String, Object> {
		private ExpirementList(List<Object> markerIdList, List<Object> pointIdList) {
			put("t", "d");
			put("markerList", markerIdList);
			put("pointList", pointIdList);
		}
	}

	protected static class MapModuleType implements Serializable {
		public enum XYOrder { XY, YX };

		public String xName, yName;
		XYOrder order;

		public MapModuleType(String xName, String yName, XYOrder order) {
			this.xName = xName;
			this.yName = yName;
			this.order = order;
		}

		public boolean isXYOrder() {
			return order == XYOrder.XY;
		}
	}
}
