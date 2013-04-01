package com.unifina.signalpath.charts;

import java.util.LinkedHashMap;

public class PointMessage extends LinkedHashMap<String,Object> {
	public PointMessage(int seriesIndex, long time, Double value) {
		super();
		put("type","p");
		put("s",seriesIndex);
		put("x",time);
		put("y",value);
	}
}
