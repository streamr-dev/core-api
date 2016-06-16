package com.unifina.signalpath.charts;

import java.io.BufferedWriter;
import java.io.File;
import java.util.Date;
import java.util.Map;

import org.apache.log4j.Logger;

import com.unifina.signalpath.ModuleOption;
import com.unifina.signalpath.ModuleOptions;
import com.unifina.signalpath.ModuleWithUI;
import com.unifina.utils.CSVWriter;
import com.unifina.utils.TimeOfDayUtil;

public abstract class Chart extends ModuleWithUI {

	protected File f;
	protected BufferedWriter outFile;
	
	Date lastHandledDate = new Date(0);
	
	protected boolean csv = false;
	
	protected boolean timeOfDayFilterEnabled = false;
	protected TimeOfDayUtil todUtil = null;
	transient protected CSVWriter csvWriter = null;
	
	private static final Logger log = Logger.getLogger(Chart.class);
	
	protected boolean hasRc = false;
	
	private void initCsv() {
		csv = true;
		csvWriter = new CSVWriter(null, globals.getGrailsApplication().getConfig(), globals.getSignalPathContext(), globals.getUserTimeZone());
	}

	@Override
	public void init() {
		canClearState = false;

		if (globals.getSignalPathContext().containsKey("csv"))
			initCsv();

		// Create visible input connections
		addDefaultInputs();
	}

	@Override
	public void initialize() {
		super.initialize();
		hasRc = (globals!=null && globals.getUiChannel()!=null);
	}
	
	protected abstract void addDefaultInputs();
	
	@Override
	public void sendOutput() {
		Date timestamp = globals.time;
		if (timestamp==null) 
			return;
		else if (csv) {
			// Time of day filter
			if (timeOfDayFilterEnabled && !todUtil.isInRange(timestamp))
				return;
			
			recordCsvString();
			csvWriter().newLine();
		}
		else {
			if (timeOfDayFilterEnabled && !todUtil.hasBaseDate())
				todUtil.setBaseDate(globals.time);
			
			if (!timeOfDayFilterEnabled || todUtil.isInRange(globals.time)) {
				record();
			}
		}
	}

	protected abstract void record();
	protected abstract void recordCsvString();
	
	@Override
	public void onDay(Date day) {
		super.onDay(day);
		
		if (csvWriter!=null)
			csvWriter().newDay();
		
		if (todUtil!=null)
			todUtil.setBaseDate(day);
	}
	
	@Override
	public void clearState() {

	}
	
	// This method can be overridden to do some post-processing on the csv
	public File getFile() {
		return csvWriter().finish();
	}
	
	@Override
	public void destroy() {
		super.destroy();
		if (csv) {
			File file = csvWriter().finish();
			if (hasRc) {
				globals.getUiChannel().push(new CSVMessage(file.getName()), uiChannelId);
			}
		}
	}
	
	@Override
	public Map<String,Object> getConfiguration() {
		Map<String,Object> config = super.getConfiguration();
		
		ModuleOptions options = ModuleOptions.get(config);
		options.add(new ModuleOption("ignoreEnabled", false, "boolean"));
		options.add(new ModuleOption("ignoreBefore", todUtil==null ? "00:00:00" : todUtil.getStartString(), "string"));
		options.add(new ModuleOption("ignoreAfter", todUtil==null ? "23:59:59" : todUtil.getEndString(), "string"));
		
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
			todUtil = new TimeOfDayUtil(begin,end,globals.getUserTimeZone());
		}
	}

	protected CSVWriter csvWriter() {
		if (csvWriter == null) {
			csvWriter = new CSVWriter(null,
					globals.getGrailsApplication().getConfig(),
					globals.getSignalPathContext(),
					globals.getUserTimeZone());
		}
		return csvWriter;
	}
}
