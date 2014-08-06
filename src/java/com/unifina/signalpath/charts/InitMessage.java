package com.unifina.signalpath.charts;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * A message initializing the chart. The message can contain a number of Series
 * as well as yAxis definitions (optional).
 * 
 * @author Henri
 */
public class InitMessage extends LinkedHashMap<String,Object> {
	public InitMessage(List<Series> series, YAxisDef yAxis) {
		super();
		put("type","init");
		put("series",series);

		if (yAxis!=null)
			put("yAxis",yAxis);
	}
}
