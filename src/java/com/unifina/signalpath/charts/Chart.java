package com.unifina.signalpath.charts;

import java.io.BufferedWriter;
import java.io.File;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.unifina.signalpath.AbstractSignalPathModule;
import com.unifina.utils.CSVWriter;
import com.unifina.utils.MapTraversal;
import com.unifina.utils.TimeOfDayUtil;

public abstract class Chart extends AbstractSignalPathModule {

	protected File f;
	protected BufferedWriter outFile;
	
	Date lastHandledDate = new Date(0);
	
	protected boolean csv = false;
	
	TimeOfDayUtil todUtil = null;
	protected CSVWriter csvWriter = null;
	
	private static final Logger log = Logger.getLogger(Chart.class);
	
	protected boolean hasRc = false;
	
	private void initCsv() {
		csv = true;
		csvWriter = new CSVWriter(null, globals.getGrailsApplication().getConfig(), globals.getSignalPathContext(), globals.getUserTimeZone());
	}

	@Override
	public void init() {
		canClearState = false;
		
		if (MapTraversal.getProperty(globals.getSignalPathContext(), "timeOfDayFilter.timeOfDayStart")!=null) {
			todUtil = new TimeOfDayUtil(
					(String)MapTraversal.getProperty(globals.getSignalPathContext(),"timeOfDayFilter.timeOfDayStart"), 
					(String)MapTraversal.getProperty(globals.getSignalPathContext(),"timeOfDayFilter.timeOfDayEnd"), 
					globals.getUserTimeZone());
		}
		
		if (globals.getSignalPathContext().containsKey("csv"))
			initCsv();

		// Create visible input connections
		addDefaultInputs();
	}

	@Override
	public void initialize() {
		super.initialize();
		hasRc = (parentSignalPath!=null && parentSignalPath.returnChannel!=null);
	}
	
	protected abstract void addDefaultInputs();
	
	@Override
	public void sendOutput() {
		Date timestamp = globals.time;
		if (timestamp==null) 
			return;
		else if (csv) {
			// Time of day filter
			if (todUtil!=null && !todUtil.isInRange(timestamp))
				return;
			
			recordCsvString();
			csvWriter.newLine();
		}
		else {
			if (todUtil!=null && !todUtil.hasBaseDate())
				todUtil.setBaseDate(globals.time);
			
			if (todUtil==null || todUtil.isInRange(globals.time)) {
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
			csvWriter.newDay();
		
		if (todUtil!=null)
			todUtil.setBaseDate(day);
	}
	
	@Override
	public void clearState() {

	}
	
	// This method can be overridden to do some post-processing on the csv
	public File getFile() {
		return csvWriter.finish();
	}
	
	@Override
	public void destroy() {
		super.destroy();
		if (csv) {
			File file = csvWriter.finish();
			if (parentSignalPath!=null && parentSignalPath.returnChannel!=null) {
				parentSignalPath.returnChannel.sendPayload(hash, new CSVMessage(file.getName(),"downloadCsv?filename="+file.getName()));
			}
		}
	}
	
	@Override
	public Map<String,Object> getConfiguration() {
		Map<String,Object> config = super.getConfiguration();
		
		Map optionsMap = (Map) config.get("options");
		if (optionsMap==null) {
			optionsMap = new LinkedHashMap<>();
			config.put("options",optionsMap);
		}

		LinkedHashMap<String, Object> ignoreBefore = new LinkedHashMap<>();
		optionsMap.put("ignoreBefore",ignoreBefore);
		
		ignoreBefore.put("value",todUtil==null ? "05:00:00" : todUtil.getStartString());
		ignoreBefore.put("type","string");
		
		LinkedHashMap<String, Object> ignoreAfter = new LinkedHashMap<>();
		optionsMap.put("ignoreAfter",ignoreAfter);
		
		ignoreAfter.put("value",todUtil==null ? "22:00:00" : todUtil.getEndString());
		ignoreAfter.put("type","string");
		
		return config;
	}
	
	@Override
	public void onConfiguration(Map config) {
		super.onConfiguration(config);
		
		Map options = (Map)config.get("options");
		
		// New
		if (options!=null) {
			// Ignore before/after
			if (MapTraversal.getProperty(options, "ignoreBefore.value")!=null) {
				String begin = MapTraversal.getProperty(options, "ignoreBefore.value").toString();
				String end = MapTraversal.getProperty(options, "ignoreAfter.value").toString();
				todUtil = new TimeOfDayUtil(begin,end,globals.getUserTimeZone());
			}
		}
	}
}
