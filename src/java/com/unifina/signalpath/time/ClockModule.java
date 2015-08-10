package com.unifina.signalpath.time;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import com.unifina.datasource.ITimeListener;
import com.unifina.signalpath.AbstractSignalPathModule;
import com.unifina.signalpath.StringOutput;
import com.unifina.signalpath.StringParameter;
import com.unifina.signalpath.TimeSeriesOutput;

public class ClockModule extends AbstractSignalPathModule implements ITimeListener {

	StringParameter format = new StringParameter(this, "format", "yyyy-MM-dd HH:mm:ss z");
	
	StringOutput date = new StringOutput(this, "date");
	TimeSeriesOutput ts = new TimeSeriesOutput(this,"timestamp");
		
	SimpleDateFormat df = null;
	
	@Override
	public void init() {
		addInput(format);
		addOutput(date);
		addOutput(ts);
	}
	
	@Override
	public void clearState() {
		df = null;
	}
	
	@Override
	public void sendOutput() {
		
	}
	
	@Override
	public void setTime(Date timestamp) {
		if(date.isConnected() && !format.getValue().isEmpty()){
			if(df == null){
				df = new SimpleDateFormat(format.getValue());
				df.setTimeZone(TimeZone.getTimeZone(globals.getUser().getTimezone()));
			}
			else if(!df.toPattern().equals(format.getValue()))
				df.applyPattern(format.getValue());
				
			date.send(df.format(timestamp));
		}
		ts.send((double)timestamp.getTime());
	}

}
