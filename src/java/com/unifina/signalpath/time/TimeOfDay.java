package com.unifina.signalpath.time;

import java.util.Date;

import com.unifina.datasource.ITimeListener;
import com.unifina.signalpath.AbstractSignalPathModule;
import com.unifina.signalpath.StringParameter;
import com.unifina.signalpath.TimeSeriesOutput;
import com.unifina.utils.TimeOfDayUtil;

public class TimeOfDay extends AbstractSignalPathModule implements ITimeListener {

	TimeSeriesOutput out = new TimeSeriesOutput(this,"out");
	
	StringParameter startTime = new StringParameter(this,"startTime","00:00:00");
	StringParameter endTime = new StringParameter(this,"endTime","23:59:59");

	transient TimeOfDayUtil util;
	
	Double currentOut = null;

	String lastStartTime = null;
	String lastEndTime = null;
	Date lastBaseDay = null;

	
	@Override
	public void init() {
		addInput(startTime);
		addInput(endTime);
		addOutput(out);
	}
	
	@Override
	public void initialize() {
		lastStartTime = startTime.getValue();
		lastEndTime = endTime.getValue();
		initUtilIfNeeded();
	}

	@Override
	public void clearState() {
		currentOut = null;
	}
	
	@Override
	public void sendOutput() {
		
	}
	
	@Override
	public void onDay(Date day) {
		super.onDay(day);
		lastBaseDay = day;
		util.setBaseDate(lastBaseDay);
	}
	
	@Override
	public void setTime(Date timestamp) {
		initUtilIfNeeded();
		if (util.isInRange(timestamp)) {
			if (currentOut==null || currentOut==0) {
				out.send(1D);
				currentOut = 1.0;
			}	
		}
		else if (currentOut==null || currentOut==1) {
			out.send(0D);
			currentOut = 0.0;
		}
	}

	private void initUtilIfNeeded() {
		if (util == null) {
			util = new TimeOfDayUtil(lastStartTime, lastEndTime, getGlobals().getUserTimeZone());
			if (lastBaseDay != null) {
				util.setBaseDate(lastBaseDay);
			}
		}
	}

}
