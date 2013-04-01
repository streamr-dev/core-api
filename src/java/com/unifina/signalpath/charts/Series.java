package com.unifina.signalpath.charts;

import java.util.LinkedHashMap;

public class Series extends LinkedHashMap<String,Object> {
	public Series(String name, int idx, boolean step, int yAxis) {
		super();
		put("name",name);
		put("idx",idx);
		put("step",step);
		put("yAxis",yAxis);
	}
}