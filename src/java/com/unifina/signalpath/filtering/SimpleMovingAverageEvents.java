package com.unifina.signalpath.filtering;

import com.unifina.math.MovingAverage;
import com.unifina.signalpath.AbstractSignalPathModule;
import com.unifina.signalpath.IntegerParameter;
import com.unifina.signalpath.TimeSeriesInput;
import com.unifina.signalpath.TimeSeriesOutput;

public class SimpleMovingAverageEvents extends AbstractSignalPathModule {
	
	IntegerParameter length = new IntegerParameter(this,"length",60);
	TimeSeriesInput input = new TimeSeriesInput(this,"in");
	TimeSeriesOutput out = new TimeSeriesOutput(this,"out");
	
	MovingAverage ma;
	
	@Override
	public void init() {
		addInput(length);
		addInput(input);
		addOutput(out);
	}
	
	@Override
	public void sendOutput() {
		if (ma==null)
			ma = new MovingAverage(length.getValue());
		
		if (ma.getLength()!=length.getValue())
			ma.setLength(length.getValue());
			
		ma.add(input.value);
		if (ma.size()==ma.getLength())
			out.send(ma.getValue());
	}
	
	@Override
	public void clearState() {
		if (ma != null)
			ma.clear();
	}
	
}
