package com.unifina.signalpath.charts;

import java.util.LinkedHashMap;

public class CSVMessage extends LinkedHashMap<String,Object> {
	public CSVMessage(String filename, String link) {
		super();
		this.put("type","csv");
		this.put("filename",filename);
		this.put("link",link);
	}
}