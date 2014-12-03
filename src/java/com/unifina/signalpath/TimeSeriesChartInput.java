package com.unifina.signalpath;

import java.util.Map;

public class TimeSeriesChartInput extends TimeSeriesInput {
	public Integer seriesIndex;
	public String seriesName;
	public Integer yAxis = 0;
	public Long previousTime = 0L;
	
	public TimeSeriesChartInput(AbstractSignalPathModule mod,String name) {
		super(mod,name);
	}
	
	@Override
	public Map<String,Object> getConfiguration() {
		Map<String,Object> config = super.getConfiguration();
		config.put("yAxis", yAxis);
		return config;
	}
	
	@Override
	public void setConfiguration(Map<String,Object> config) {
		super.setConfiguration(config);
		if (config.containsKey("yAxis"))
			yAxis = Integer.parseInt(config.get("yAxis").toString());
	}

}
