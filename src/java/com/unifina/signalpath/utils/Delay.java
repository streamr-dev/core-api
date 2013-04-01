package com.unifina.signalpath.utils;

import java.util.ArrayDeque;

import com.unifina.signalpath.AbstractSignalPathModule;
import com.unifina.signalpath.IntegerParameter;
import com.unifina.signalpath.TimeSeriesInput;
import com.unifina.signalpath.TimeSeriesOutput;

public class Delay extends AbstractSignalPathModule {

	IntegerParameter length = new IntegerParameter(this,"delayEvents",1);
	TimeSeriesInput input = new TimeSeriesInput(this,"in");
	TimeSeriesOutput out = new TimeSeriesOutput(this,"out");
	
	ArrayDeque<Double> window = new ArrayDeque<>();
	
	@Override
	public void init() {
		addInput(length);
		addInput(input);
		addOutput(out);
	}
	
	@Override
	public void sendOutput() {
		while (window.size()>length.getValue())
			window.removeFirst();
		
		if (window.size()==length.getValue()) {
			out.send(window.removeFirst());
		}
		
		window.add(input.value);
	}
	
	@Override
	public void clearState() {
		window.clear();
	}

}
