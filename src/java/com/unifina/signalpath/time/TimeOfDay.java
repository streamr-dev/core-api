package com.unifina.signalpath.time;

import com.unifina.datasource.ITimeListener;
import com.unifina.signalpath.AbstractSignalPathModule;
import com.unifina.signalpath.StringParameter;
import com.unifina.signalpath.TimeSeriesOutput;
import com.unifina.utils.DateRange;

import java.util.Date;
import java.util.TimeZone;

public class TimeOfDay extends AbstractSignalPathModule implements ITimeListener {

	private final TimezoneParameter tz = new TimezoneParameter(this, "timezone", TimeZone.getTimeZone("UTC"));

	TimeSeriesOutput out = new TimeSeriesOutput(this,"out");

	StringParameter startTime = new StringParameter(this,"startTime","00:00:00");
	StringParameter endTime = new StringParameter(this,"endTime","23:59:59");

	transient DateRange range;

	Double currentOut = null;

	String lastStartTime = null;
	String lastEndTime = null;
	Date lastBaseDay = null;


	@Override
	public void init() {
		addInput(tz);
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
		range.setBaseDate(lastBaseDay);
	}

	@Override
	public void setTime(Date timestamp) {
		initUtilIfNeeded();
		if (range.isInRange(timestamp)) {
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

	@Override
	public int tickRateInSec() {
		return 1;
	}

	private void initUtilIfNeeded() {
		if (range == null) {
			range = new DateRange(lastStartTime, lastEndTime);
			if (lastBaseDay != null) {
				range.setBaseDate(lastBaseDay);
			}
		}
	}
}
