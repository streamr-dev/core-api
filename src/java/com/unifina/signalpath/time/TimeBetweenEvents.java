package com.unifina.signalpath.time;

import java.util.Date;

import com.unifina.signalpath.AbstractSignalPathModule;
import com.unifina.signalpath.Input;
import com.unifina.signalpath.TimeSeriesOutput;

public class TimeBetweenEvents extends AbstractSignalPathModule {
	
	Input<Object> in = new Input<Object>(this, "in", "Object");

	TimeSeriesOutput out = new TimeSeriesOutput(this,"ms");
	
	Date date = null;
	Long lastTs = null;

	@Override
	public void init() {
		addInput(in);
		addOutput(out);
	}
	
	@Override
	public void initialize() {
	}
	
	@Override
	public void clearState() {
		date = null;
		lastTs = null;
	}
	
	@Override
	public void sendOutput() {
		date = getGlobals().time;
		Long ts = date.getTime();
		if(lastTs != null){
			out.send((double)(ts-lastTs));
		}
		lastTs = ts;
	}

}
