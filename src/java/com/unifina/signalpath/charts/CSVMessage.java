package com.unifina.signalpath.charts;

import java.util.LinkedHashMap;

class CSVMessage extends LinkedHashMap<String,Object> {
	CSVMessage(String filename) {
		super();
		this.put("type","csv");
		this.put("filename",filename);
	}
}