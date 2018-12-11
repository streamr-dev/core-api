package com.unifina.signalpath.time;

import com.unifina.service.SerializationService;
import com.unifina.signalpath.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class DateConversion extends AbstractSignalPathModule implements TimezoneModule {

	StringParameter tz = new StringParameter(this, "timezone", "UTC");
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
		tz.receive(this.getTimezone().getID());
	}

	@Override
	public void sendOutput() {
		if(pattern.getValue() != null && !df.toPattern().equals(pattern.getValue())){
			df.applyPattern(pattern.getValue());
		}

		if (tz.getValue() != null) {
			TimeZone timeZone = this.getTimezone();
			if (timeZone != null) {
				cal.setTimeZone(timeZone);
				df.setTimeZone(timeZone);
			}
		}

		Date date = dateFromValue(dateIn.getValue());

		cal.setTime(date);

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

	@Override
	public void clearState() {
		df = null;
		cal = null;
		ensureState();
	}

	@Override
	public void afterDeserialization(SerializationService serializationService) {
		super.afterDeserialization(serializationService);
		ensureState();
	}

	private Date dateFromValue(Object value) {
		Date date;
		if (value instanceof Double) {
			date = new Date(Math.round((double) value));
		} else if (value instanceof Date) {
			date = (Date) value;
		} else if (value instanceof String) {
			try {
				date = df.parse((String) value);
			} catch (ParseException e) {
				throw new RuntimeException("The input date is not in the given format!", e);
			}
		} else {
			throw new RuntimeException("Input date of unexpected type: " + value.getClass());
		}
		return date;
	}

	private void ensureState() {
		final TimeZone timezone = this.getTimezone();
		if (cal == null) {
			cal = Calendar.getInstance(timezone);
		}
		if (df == null) {
			df = new SimpleDateFormat();
			df.setTimeZone(timezone);
		}
	}

	@Override
	public void setTimezone(String timezone) {
		tz.receive(timezone);
	}

	@Override
	public TimeZone getTimezone() {
		return TimeZone.getTimeZone(tz.getValue());
	}
}
