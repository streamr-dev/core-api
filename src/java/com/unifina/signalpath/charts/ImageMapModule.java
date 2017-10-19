package com.unifina.signalpath.charts;

import com.unifina.signalpath.ModuleOption;
import com.unifina.signalpath.ModuleOptions;
import grails.util.Holders;
import org.codehaus.groovy.grails.web.mapping.LinkGenerator;

import java.util.Map;

import static java.util.Collections.singletonMap;


public class ImageMapModule extends MapModule {
	private static final String DEFAULT_IMAGE_URL = "/images/imageMapModule/defaultBackground.png";

	private String customImageUrl;

	public ImageMapModule() {
		super(200, 300, -3, 5, 0, false);
	}

	@Override
	public String getWebcomponentName() {
		return "streamr-map";
	}

	@Override
	protected MapModuleType getMapModuleType() {
		return new MapModuleType("x", "y", MapModuleType.XYOrder.XY);
	}

	@Override
	protected Double xToLongitude(Double x) {
		return x;
	}

	@Override
	protected Double yToLatitude(Double y) {
		return 1-y; // convention for images is that (0,0) is top-left corner
	}

	@Override
	public Map<String, Object> getConfiguration() {
		Map<String, Object> config = super.getConfiguration();
		ModuleOptions options = ModuleOptions.get(config);
		options.addIfMissing(ModuleOption.createString("customImageUrl", customImageUrl));
		return config;
	}

	@Override
	protected void onConfiguration(Map<String, Object> config) {
		customImageUrl = Holders.getApplicationContext()
			.getBean(LinkGenerator.class)
			.link(singletonMap("uri", DEFAULT_IMAGE_URL));

		super.onConfiguration(config);
		ModuleOptions options = ModuleOptions.get(config);
		if (options.containsKey("customImageUrl")) {
			if (options.getOption("customImageUrl").getString().isEmpty()) {
				throw new RuntimeException("Custom image url cannot be empty!");
			}
			customImageUrl = options.getOption("customImageUrl").getString();
		}
	}
}
