package com.unifina.signalpath.statistics;

import com.unifina.signalpath.AbstractSignalPathModule;
import com.unifina.signalpath.IntegerParameter;
import com.unifina.signalpath.TimeSeriesInput;
import com.unifina.signalpath.TimeSeriesOutput;

public class StandardDeviation extends AbstractSignalPathModule {

	IntegerParameter length = new IntegerParameter(this,"length",60);
	IntegerParameter minSamples = new IntegerParameter(this,"minSamples",1);
	TimeSeriesInput input = new TimeSeriesInput(this,"in");
	TimeSeriesOutput out = new TimeSeriesOutput(this,"out");
	
	com.unifina.math.StandardDeviation sd;
	
	@Override
	public void init() {
		addInput(length);
		addInput(minSamples);
		addInput(input);
		addOutput(out);
	}

	@Override
	public void initialize() {

	}
	
	@Override
	public void sendOutput() {
		if (sd==null)
			sd = new com.unifina.math.StandardDeviation(length.value);
		
		if (length.value!=sd.getLength())
			sd.setLength(length.value);
		
		sd.add(input.value);
		
		if (sd.size()>=minSamples.value)
			out.send(sd.getValue());
	}

	@Override
	public void clearState() {
		if (sd!=null)
			sd.clear();
	}

}
