package com.unifina.signalpath.charts;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import com.unifina.signalpath.AbstractSignalPathModule;
import com.unifina.signalpath.DoubleParameter;
import com.unifina.signalpath.IReturnChannel;
import com.unifina.signalpath.TimeSeriesInput;
import com.unifina.utils.MapTraversal;

public class Gauge extends AbstractSignalPathModule {

	DoubleParameter min = new DoubleParameter(this,"min",-1D);
	DoubleParameter max = new DoubleParameter(this,"max",1D);
	TimeSeriesInput value = new TimeSeriesInput(this,"value");
	
	boolean initSent = false;
	IReturnChannel rc = null;
	
	Double reportedMin = null;
	Double reportedMax = null;
	
	String title = "";
	boolean labels = true;
	String labelFormatter = "";
	
	@Override
	public void init() {
		addInput(min);
		addInput(max);
		addInput(value);
	}

	@Override
	public void initialize() {
		super.initialize();
		if (parentSignalPath!=null && parentSignalPath.getReturnChannel()!=null)
			rc = parentSignalPath.getReturnChannel();
	}
	
	@Override
	public void sendOutput() {
		if (rc!=null) {
			if (!initSent) {
				HashMap<String,Object> msg = new HashMap<>();
				msg.put("type", "init");
				msg.put("v",value.value);
				msg.put("min", min.getValue());
				msg.put("max", max.getValue());
				msg.put("title", title);
				rc.sendPayload(hash, msg);
				initSent = true;
			}
			else {
				HashMap<String,Object> msg = new HashMap<>();
				msg.put("type", "u");
				msg.put("v",value.value);
				if (reportedMin==null || reportedMin!=min.getValue()) {
					msg.put("min", min.getValue());
					reportedMin = min.getValue();
				}
				if (reportedMax==null || reportedMax!=max.getValue()) {
					msg.put("max", max.getValue());
					reportedMax = max.getValue();
				}
				rc.sendPayload(hash, msg);
			}
		}
	}

	@Override
	public void clearState() {
		
	}
	
	@Override
	public Map<String, Object> getConfiguration() {
		Map<String,Object> config = super.getConfiguration();
		Map<String,Object> options = (Map<String,Object>) config.get("options");
		if (options==null) {
			options = new LinkedHashMap<>();
			config.put("options",options);
		}
		
		LinkedHashMap<String, Object> titleOption = new LinkedHashMap<>();
		options.put("title",titleOption);
		titleOption.put("value",title);
		titleOption.put("type","string");
		 
		LinkedHashMap<String,Object> labelsOption = new LinkedHashMap<>();
		options.put("labels",labelsOption);
		labelsOption.put("value",labels);
		labelsOption.put("type","boolean");
		
		LinkedHashMap<String,Object> labelFormatterOption = new LinkedHashMap<>();
		options.put("labelFormatter",labelFormatterOption);
		labelFormatterOption.put("value",labelFormatter);
		labelFormatterOption.put("type","string");
		
		return config;
	}

	@Override
	protected void onConfiguration(Map<String, Object> config) {
		super.onConfiguration(config);
		
		Map<String,Object> options = (Map<String,Object>) config.get("options");
		
		if (options!=null) {
			if (MapTraversal.getProperty(options, "title.value")!=null) {
				title = MapTraversal.getString(options, "title.value");
			}
			if (MapTraversal.getProperty(options, "labels.value")!=null) {
				labels = MapTraversal.getBoolean(options, "labels.value");
			}
			if (MapTraversal.getProperty(options, "labelFormatter.value")!=null) {
				labelFormatter = MapTraversal.getString(options, "labelFormatter.value");
			}
		}
	}
	
}
