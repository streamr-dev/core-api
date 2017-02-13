package com.unifina.signalpath.charts;

import com.unifina.signalpath.*;
import grails.util.Holders;
import org.codehaus.groovy.grails.web.mapping.LinkGenerator;

import java.util.HashMap;


public class ImageMapModule extends MapModule {

	private String customImageUrl;
	private int customImageWidth = 600;
	private int customImageHeight = 400;
	private int minZoom = -3;
	private int maxZoom = 5;
	private double centerLat = 200;
	private double centerLng = 300;
	private int zoom = 0;
	private boolean autoZoom = false;

	public ImageMapModule() {
		HashMap<String, String> url = new HashMap<>();
		url.put("uri", "/images/imageMapModule/defaultBackground.png");
		customImageUrl = Holders.getGrailsApplication().getMainContext().getBean(LinkGenerator.class).link(url);
	}

	@Override
	public String getWebcomponentName() {
		return "streamr-image-map";
	}

	@Override
	public java.util.Map<String, Object> getConfiguration() {
		java.util.Map<String, Object> config = super.getConfiguration();

		ModuleOptions options = ModuleOptions.get(config);
		options.addIfMissing(new ModuleOption("customImageHeight", customImageHeight, ModuleOption.OPTION_INTEGER));
		options.addIfMissing(new ModuleOption("customImageUrl", customImageUrl, ModuleOption.OPTION_STRING));
		options.addIfMissing(new ModuleOption("customImageWidth", customImageWidth, ModuleOption.OPTION_INTEGER));
		options.addIfMissing(new ModuleOption("centerLat", centerLat, ModuleOption.OPTION_DOUBLE));
		options.addIfMissing(new ModuleOption("centerLng", centerLng, ModuleOption.OPTION_DOUBLE));
		options.add(new ModuleOption("minZoom", minZoom, ModuleOption.OPTION_INTEGER));
		options.add(new ModuleOption("maxZoom", maxZoom, ModuleOption.OPTION_INTEGER));
		options.add(new ModuleOption("zoom", zoom, ModuleOption.OPTION_INTEGER));
		options.add(new ModuleOption("autoZoom", autoZoom, ModuleOption.OPTION_BOOLEAN));
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

		if (options.containsKey("customImageHeight")) {
			customImageHeight = options.getOption("customImageHeight").getInt();
		}

		if (options.containsKey("customImageUrl")) {
			customImageUrl = options.getOption("customImageUrl").getString();
		}

		if (options.containsKey("customImageWidth")) {
			customImageWidth = options.getOption("customImageWidth").getInt();
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

	}
}
