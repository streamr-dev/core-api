package com.unifina.signalpath.charts;

import com.unifina.signalpath.ModuleOption;
import com.unifina.signalpath.ModuleOptions;
import com.unifina.signalpath.ModuleWithUI;
import com.unifina.utils.TimeOfDayUtil;

import java.util.Date;
import java.util.Map;

public abstract class Chart extends ModuleWithUI {
	private boolean timeOfDayFilterEnabled = false;
	private TimeOfDayUtil todUtil = null;
	private String dataGrouping = "min/max";

	@Override
	public void init() {
		canClearState = false;
		resendAll = false;
		resendLast = 500;
	}

	@Override
	public void initialize() {
		super.initialize();
	}
	
	@Override
	public void sendOutput() {
		Date timestamp = getGlobals().time;
		if (timestamp != null) {
			if (timeOfDayFilterEnabled && !todUtil.hasBaseDate()) {
				todUtil.setBaseDate(getGlobals().time);
			}
			if (!timeOfDayFilterEnabled || todUtil.isInRange(getGlobals().time)) {
				record();
			}
		}
	}

	protected abstract void record();
	
	@Override
	public void onDay(Date day) {
		super.onDay(day);

		if (todUtil != null) {
			todUtil.setBaseDate(day);
		}
	}
	
	@Override
	public void clearState() {}
	
	@Override
	public Map<String,Object> getConfiguration() {
		Map<String,Object> config = super.getConfiguration();
		
		ModuleOptions options = ModuleOptions.get(config);
		options.add(new ModuleOption("ignoreEnabled", false, "boolean"));
		options.add(new ModuleOption("ignoreBefore", todUtil==null ? "00:00:00" : todUtil.getStartString(), "string"));
		options.add(new ModuleOption("ignoreAfter", todUtil==null ? "23:59:59" : todUtil.getEndString(), "string"));

		ModuleOption dataGroupingOption = ModuleOption.createString("dataGrouping", dataGrouping);
		dataGroupingOption.addPossibleValue("max positive/min negative", "min/max");
		dataGroupingOption.addPossibleValue("average", "average");
		dataGroupingOption.addPossibleValue("first", "open");
		dataGroupingOption.addPossibleValue("last", "close");
		dataGroupingOption.addPossibleValue("max", "high");
		dataGroupingOption.addPossibleValue("min", "low");
		options.addIfMissing(dataGroupingOption);

		return config;
	}
	
	@Override
	public void onConfiguration(Map config) {
		super.onConfiguration(config);
		
		ModuleOptions options = ModuleOptions.get(config);
		
		timeOfDayFilterEnabled = options.getOption("ignoreEnabled")!=null && options.getOption("ignoreEnabled").getBoolean();
		
		if (timeOfDayFilterEnabled && options.getOption("ignoreBefore")!=null) {
			String begin = options.getOption("ignoreBefore").getString();
			String end = options.getOption("ignoreAfter").getString();
			todUtil = new TimeOfDayUtil(begin,end, getGlobals().getUserTimeZone());
		}

		if (options.getOption("dataGrouping") != null) {
			dataGrouping = options.getOption("dataGrouping").toString();
		}
	}
}