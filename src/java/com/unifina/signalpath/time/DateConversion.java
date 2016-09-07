package com.unifina.signalpath.time;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import com.unifina.signalpath.AbstractSignalPathModule;
import com.unifina.signalpath.Input;
import com.unifina.signalpath.StringOutput;
import com.unifina.signalpath.StringParameter;
import com.unifina.signalpath.TimeSeriesOutput;

public class DateConversion extends AbstractSignalPathModule {
	
	// The default value of tz is added in the initialize() method
	StringParameter tz = new StringParameter(this, "timezone", "");
	StringParameter pattern = new StringParameter(this, "format", "yyyy-MM-dd HH:mm:ss z");
	
	Input<Object> dateIn = new Input<>(this, "date", "Date Double String");

	StringOutput dateOut = new StringOutput(this, "date");
	TimeSeriesOutput tsOut = new TimeSeriesOutput(this,"ts");
	StringOutput wdOut = new StringOutput(this, "dayOfWeek");
	TimeSeriesOutput yearsOut = new TimeSeriesOutput(this, "years");
	TimeSeriesOutput monthsOut = new TimeSeriesOutput(this, "months");
	TimeSeriesOutput daysOut = new TimeSeriesOutput(this, "days");
	TimeSeriesOutput hoursOut = new TimeSeriesOutput(this, "hours");
	TimeSeriesOutput minutesOut = new TimeSeriesOutput(this, "minutes");
	TimeSeriesOutput secondsOut = new TimeSeriesOutput(this, "seconds");
	TimeSeriesOutput msOut = new TimeSeriesOutput(this, "milliseconds");

	Date date = null;
	transient Calendar cal = null;
	transient SimpleDateFormat df = null;
	
	@Override
	public void init() {
		addInput(tz);
		addInput(pattern);
		addInput(dateIn);
		
		addOutput(dateOut);
		addOutput(tsOut);
		addOutput(wdOut);
		addOutput(yearsOut);
		addOutput(monthsOut);
		addOutput(daysOut);
		addOutput(hoursOut);
		addOutput(minutesOut);
		addOutput(secondsOut);
		addOutput(msOut);

		ensureState();
	}

	@Override
	public void initialize() {
		super.initialize();
		if (getGlobals().getUser()!=null)
			tz.receive(getGlobals().getUser().getTimezone());
	}
	
	@Override
	public void sendOutput() {
		ensureState();
		if(pattern.getValue() != null && !df.toPattern().equals(pattern.getValue())){
			df.applyPattern(pattern.getValue());
		}

		if(dateIn.getValue() instanceof Double){
			date = new Date(Math.round((double)dateIn.getValue()));
		} else if(dateIn.getValue() instanceof Date){
			date = (Date)dateIn.getValue();
		} else if(dateIn.getValue() instanceof String){
			try {
				date = df.parse((String)dateIn.getValue());
			} catch (ParseException e) {
				throw new RuntimeException("The input date is not in the given format!",e);
			}
		}
		cal.setTime(date);
		if(tz.getValue() != null)
			cal.setTimeZone(TimeZone.getTimeZone(tz.getValue()));

		tsOut.send((double)date.getTime());
		wdOut.send(cal.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.US));
		yearsOut.send(cal.get(Calendar.YEAR));
		monthsOut.send(cal.get(Calendar.MONTH)+1);
		daysOut.send(cal.get(Calendar.DAY_OF_MONTH));
		hoursOut.send(cal.get(Calendar.HOUR_OF_DAY));
		minutesOut.send(cal.get(Calendar.MINUTE));
		secondsOut.send(cal.get(Calendar.SECOND));
		msOut.send(cal.get(Calendar.MILLISECOND));

		if(dateOut.isConnected()){
			dateOut.send(df.format(date));
		}
	}

	private void ensureState() {
		if (cal == null) {
			cal = Calendar.getInstance();

			if (getGlobals().getUser()!=null)
				cal.setTimeZone(TimeZone.getTimeZone(getGlobals().getUser().getTimezone()));
		}

		if (df == null) {
			df = new SimpleDateFormat();

			if (getGlobals().getUser()!=null)
				df.setTimeZone(TimeZone.getTimeZone(getGlobals().getUser().getTimezone()));
		}
	}

	@Override
	public void clearState() {
		df = null;
		cal = null;
		date = null;
	}
}