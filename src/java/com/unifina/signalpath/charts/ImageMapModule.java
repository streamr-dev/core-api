package com.unifina.signalpath.charts;

import com.unifina.signalpath.ModuleOption;
import com.unifina.signalpath.ModuleOptions;
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
		return "streamr-image-map";
	}

	@Override
	public Map<String, Object> getConfiguration() {
		Map<String, Object> config = super.getConfiguration();
		ModuleOptions options = ModuleOptions.get(config);
		options.addIfMissing(ModuleOption.createString("customImageUrl", customImageUrl));
		return config;
	}

	@Override
	protected void onConfiguration(java.util.Map<String, Object> config) {
		customImageUrl = getGlobals().getBean(LinkGenerator.class).link(singletonMap("uri", DEFAULT_IMAGE_URL));

		super.onConfiguration(config);
		ModuleOptions options = ModuleOptions.get(config);
		if (options.containsKey("customImageUrl")) {
			customImageUrl = options.getOption("customImageUrl").getString();
		}
	}
}
