package com.unifina.signalpath.simplemath;

import java.util.LinkedList;

import com.unifina.signalpath.AbstractSignalPathModule;
import com.unifina.signalpath.IntegerParameter;
import com.unifina.signalpath.TimeSeriesInput;
import com.unifina.signalpath.TimeSeriesOutput;

public class Sum extends AbstractSignalPathModule {

	IntegerParameter windowLength = new IntegerParameter(this,"windowLength",0);
	IntegerParameter minSamples = new IntegerParameter(this,"minSamples",1);
	TimeSeriesInput input = new TimeSeriesInput(this,"in");
	TimeSeriesOutput out = new TimeSeriesOutput(this,"out");

	LinkedList<Double> values = new LinkedList<>();
	com.unifina.math.Sum sum = null;
	int count = 0;
	
	@Override
	public void init() {
		addInput(windowLength);
		addInput(minSamples);
		addInput(input);
		addOutput(out);
	}
	
	@Override
	public void sendOutput() {
		if (sum==null)
			sum = new com.unifina.math.Sum(windowLength.getValue());
		else sum.setLength(windowLength.getValue());

		sum.add(input.getValue());
		count++;

		if (count>=minSamples.getValue())
			out.send(sum.getValue());
	}

	@Override
	public void clearState() {
		values.clear();
		sum.clear();
		count = 0;
	}
	
}
