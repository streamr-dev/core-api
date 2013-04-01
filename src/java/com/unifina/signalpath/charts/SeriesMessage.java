package com.unifina.signalpath.charts;

import java.util.LinkedHashMap;

/**
 * A message for adding new Series to the chart on-the-fly.
 * @author Henri
 */
public class SeriesMessage extends LinkedHashMap<String,Object> {
	public SeriesMessage(Series series) {
		super();
		put("type","s");
		put("series",series);
	}
}
