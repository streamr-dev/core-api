package com.unifina.signalpath.charts;

import java.util.LinkedHashMap;

public class Axis extends LinkedHashMap<String,Object> {
	public Axis(String name) {
		put("name",name);
	}
}