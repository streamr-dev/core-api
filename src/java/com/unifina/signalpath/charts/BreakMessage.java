package com.unifina.signalpath.charts;

import java.util.LinkedHashMap;

public class BreakMessage extends LinkedHashMap<String,Object> {
	public BreakMessage(int seriesIndex) {
		super();
		put("type","b");
		put("s",seriesIndex);
	}
}