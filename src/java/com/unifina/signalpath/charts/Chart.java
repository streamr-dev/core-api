package com.unifina.signalpath.charts;

import com.unifina.signalpath.ModuleOption;
import com.unifina.signalpath.ModuleOptions;
import com.unifina.signalpath.ModuleWithUI;

import java.util.Date;
import java.util.Map;

public abstract class Chart extends ModuleWithUI {
	private String dataGrouping = "min/max";

	@Override
	public void init() {
		canClearState = false;
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
			record();
		}
	}

	protected abstract void record();

	@Override
	public void onDay(Date day) {
		super.onDay(day);
	}

	@Override
	public void clearState() {
	}

	@Override
	public Map<String, Object> getConfiguration() {
		Map<String, Object> config = super.getConfiguration();

		ModuleOptions options = ModuleOptions.get(config);
		options.addIfMissing(ModuleOption.createString("dataGrouping", dataGrouping)
				.addPossibleValue("max positive/min negative", "min/max")
				.addPossibleValue("average", "average")
				.addPossibleValue("first", "open")
				.addPossibleValue("last", "close")
				.addPossibleValue("max", "high")
				.addPossibleValue("min", "low")
		);

		return config;
	}

	@Override
	public void onConfiguration(Map config) {
		super.onConfiguration(config);

		ModuleOptions options = ModuleOptions.get(config);

		if (ModuleOption.validate(options.getOption("dataGrouping"))) {
			dataGrouping = options.getOption("dataGrouping").getString();
		}
	}
}
