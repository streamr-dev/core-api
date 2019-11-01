package com.unifina.utils;

public enum Webcomponent {
	STREAMR_CLIENT("streamr-client"),
	STREAMR_WIDGET("streamr-widget"),
	STREAMR_INPUT("streamr-input"),
	STREAMR_LABEL("streamr-label"),
	STREAMR_CHART("streamr-chart"),
	STREAMR_HEATMAP("streamr-heatmap"),
	STREAMR_TABLE("streamr-table"),
	STREAMR_BUTTON("streamr-button"),
	STREAMR_SWITCHER("streamr-switcher"),
	STREAMR_TEXT_FIELD("streamr-text-field"),
	STREAMR_MAP("streamr-map");

	String name;

	Webcomponent(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public static Webcomponent getByName(String s) {
		for (Webcomponent w : Webcomponent.values()) {
			if (w.getName().equals(s)) {
				return w;
			}
		}
		return null;
	}
}
