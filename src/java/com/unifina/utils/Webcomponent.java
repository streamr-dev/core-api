package com.unifina.utils;

public enum Webcomponent {
	STREAMR_CLIENT ("streamr-client"),
	STREAMR_CHART ("streamr-chart"),
	STREAMR_MAP ("streamr-map"),
	STREAMR_HEATMAP ("streamr-heatmap"),
	STREAMR_LABEL ("streamr-label"),
	STREAMR_BUTTON ("streamr-button"),
	STREAMR_SWITCH ("streamr-switch"),
	STREAMR_TEXT_FIELD ("streamr-text-field"),
	STREAMR_TABLE ("streamr-table");

	private final String name;

	Webcomponent(String s) {
		name = s;
	}

	public static Webcomponent getByName(String s) {
		for (Webcomponent w : values()) {
			if (w.name.equals(s)) {
				return w;
			}
		}
		throw new IllegalArgumentException("No Streamr webcomponent found for " + s);
	}

	public String getName() {
		return name;
	}

	public String toString() {
		return this.name;
	}
}
