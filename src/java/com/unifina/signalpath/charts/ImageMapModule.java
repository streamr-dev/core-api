package com.unifina.signalpath.charts;

import com.unifina.signalpath.*;
import org.codehaus.groovy.grails.web.mapping.LinkGenerator;

import java.util.Collections;


public class ImageMapModule extends MapModule {
	private static final String DEFAULT_IMAGE_URL = "/images/imageMapModule/defaultBackground.png";

	private String customImageUrl;
	private int customImageWidth = 600;
	private int customImageHeight = 400;

	public ImageMapModule() {
		super(200, 300, -3, 5, 0, false);
	}

	@Override
	public String getWebcomponentName() {
		return "streamr-image-map";
	}

	@Override
	public java.util.Map<String, Object> getConfiguration() {
		customImageUrl = getGlobals().getBean(LinkGenerator.class).link(Collections.singletonMap("uri", DEFAULT_IMAGE_URL));

		java.util.Map<String, Object> config = super.getConfiguration();

		ModuleOptions options = ModuleOptions.get(config);
		options.addIfMissing(ModuleOption.createString("customImageUrl", customImageUrl));
		options.addIfMissing(ModuleOption.createInt("customImageWidth", customImageWidth));
		options.addIfMissing(ModuleOption.createInt("customImageHeight", customImageHeight));

		return config;
	}

	@Override
	protected void onConfiguration(java.util.Map<String, Object> config) {
		super.onConfiguration(config);
		ModuleOptions options = ModuleOptions.get(config);

		if (options.containsKey("customImageUrl")) {
			customImageUrl = options.getOption("customImageUrl").getString();
		}
		if (options.containsKey("customImageWidth")) {
			customImageWidth = options.getOption("customImageWidth").getInt();
		}
		if (options.containsKey("customImageHeight")) {
			customImageHeight = options.getOption("customImageHeight").getInt();
		}
	}
}
