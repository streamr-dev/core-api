package com.unifina.signalpath.charts;

import java.util.HashMap;
import java.util.Map;

import com.unifina.push.PushChannel;
import com.unifina.signalpath.AbstractSignalPathModule;
import com.unifina.signalpath.DoubleParameter;
import com.unifina.signalpath.ModuleWithUI;
import com.unifina.signalpath.TimeSeriesInput;
import com.unifina.utils.MapTraversal;

public class Gauge extends ModuleWithUI {

	DoubleParameter min = new DoubleParameter(this,"min",-1D);
	DoubleParameter max = new DoubleParameter(this,"max",1D);
	TimeSeriesInput value = new TimeSeriesInput(this,"value");
	
	boolean initSent = false;
	PushChannel rc = null;
	
	String title = "";
	String titleStyle = "";
	boolean labels = true;
	String labelFormatter = "";
	String labelStyle = "";
	
	@Override
	public void init() {
		addInput(min);
		addInput(max);
		addInput(value);
	}

	@Override
	public void initialize() {
		super.initialize();
		if (globals!=null && globals.getUiChannel()!=null)
			rc = globals.getUiChannel();
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
				rc.push(msg, uiChannelId);
				initSent = true;
			}
			else {
				HashMap<String,Object> msg = new HashMap<>();
				msg.put("type", "u");
				msg.put("v",value.value);
				rc.push(msg, uiChannelId);
			}
		}
	}

	@Override
	public void clearState() {
		
	}
	
	@Override
	public Map<String, Object> getConfiguration() {
		Map<String,Object> config = super.getConfiguration();

		addOption(config, "title", "string", title);
		addOption(config, "titleStyle", "string", titleStyle);
		addOption(config, "labels", "boolean", labels);
		addOption(config, "labelFormatter", "string", labelFormatter);
		addOption(config, "labelStyle", "string", labelStyle);
		
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
			if (getOption(config, "titleStyle")!=null)
				titleStyle = getOption(config, "titleStyle").toString();
			if (MapTraversal.getProperty(options, "labels.value")!=null) {
				labels = MapTraversal.getBoolean(options, "labels.value");
			}
			if (MapTraversal.getProperty(options, "labelFormatter.value")!=null) {
				labelFormatter = MapTraversal.getString(options, "labelFormatter.value");
			}
			if (MapTraversal.getProperty(options, "labelStyle.value")!=null) {
				labelStyle = MapTraversal.getString(options, "labelStyle.value");
			}
		}
	}
	
}
