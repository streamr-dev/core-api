package com.unifina.signalpath.time;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import com.unifina.signalpath.AbstractSignalPathModule;
import com.unifina.signalpath.Input;
import com.unifina.signalpath.StringOutput;
import com.unifina.signalpath.StringParameter;
import com.unifina.signalpath.TimeSeriesInput;
import com.unifina.signalpath.TimeSeriesOutput;

public class DateConversion extends AbstractSignalPathModule {
	
	// The default value of tz is added in the initialize() method
	StringParameter tz = new StringParameter(this, "timezone", "");
	StringParameter format = new StringParameter(this, "format", "yyyy-MM-dd HH:mm:ss z");
	
	Input<Object> dateIn = new Input<>(this, "date", "Date Double");

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
	Calendar cal = Calendar.getInstance();
	SimpleDateFormat df = null;
	
	@Override
	public void init() {
		addInput(tz);
		addInput(format);
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
	}
	
	@Override
	public void initialize() {
		super.initialize();
		tz.receive(globals.getUser().getTimezone());
	}
	
	@Override
	public void sendOutput() {
		if(dateIn.getValue() instanceof Double){
			date = new Date(Math.round((double)dateIn.getValue()));
		} else if(dateIn.getValue() instanceof Date){
			date = (Date)dateIn.getValue();
		}
		cal.setTimeZone(TimeZone.getTimeZone(tz.getValue()));
		cal.setTime(date);
		
		if(dateOut.isConnected() && !format.getValue().isEmpty()){
			if(df == null || !df.toPattern().equals(format.getValue()) || !df.getTimeZone().equals(TimeZone.getTimeZone(tz.getValue()))){
				df = new SimpleDateFormat(format.getValue());
				df.setTimeZone(TimeZone.getTimeZone(tz.getValue()));
			}
			dateOut.send(df.format(date));
		}
		tsOut.send((double)date.getTime());
		wdOut.send(cal.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.US));
		yearsOut.send(cal.get(Calendar.YEAR));
		monthsOut.send(cal.get(Calendar.MONTH)+1);
		daysOut.send(cal.get(Calendar.DAY_OF_MONTH));
		hoursOut.send(cal.get(Calendar.HOUR));
		minutesOut.send(cal.get(Calendar.MINUTE));
		secondsOut.send(cal.get(Calendar.SECOND));
		msOut.send(cal.get(Calendar.MILLISECOND));
	}
	
	@Override
	public void clearState() {
		df = null;
		cal = Calendar.getInstance();
		date = null;
	}
}
