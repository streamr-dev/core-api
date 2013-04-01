package com.unifina.signalpath.simplemath;

import java.util.LinkedList;

import com.unifina.signalpath.AbstractSignalPathModule;
import com.unifina.signalpath.IntegerParameter;
import com.unifina.signalpath.TimeSeriesInput;
import com.unifina.signalpath.TimeSeriesOutput;

public class Sum extends AbstractSignalPathModule {

	IntegerParameter windowLength = new IntegerParameter(this,"windowLength",0);
	TimeSeriesInput input = new TimeSeriesInput(this,"in");
	TimeSeriesOutput out = new TimeSeriesOutput(this,"out");

	LinkedList<Double> values = new LinkedList<>();
	double sum = 0;
	
	@Override
	public void init() {
		addInput(windowLength);
		addInput(input);
		addOutput(out);
	}
	
	@Override
	public void sendOutput() {
		
		if (windowLength.getValue() > 0) {
			while (values.size() >= windowLength.getValue()) {
				sum -= values.poll();
			}
			values.add(input.value);
		}
		
		sum += input.value;

		out.send(sum);
	}

	@Override
	public void clearState() {
		values.clear();
		sum = 0;
	}
	
}
