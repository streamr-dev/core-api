package com.unifina.signalpath.charts;

import java.io.BufferedWriter;
import java.io.File;
import java.util.Date;

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
		else record();
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
}
