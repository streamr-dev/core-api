package com.unifina.signalpath.utils;

import com.unifina.signalpath.*;
import com.unifina.utils.Globals;

import java.util.ArrayList;

public class RateLimit extends AbstractSignalPathModule {

	Input<Object> input = new Input<>(this, "input", "object");

	IntegerParameter time = new IntegerParameter(this, "timeInMillis", 1000);
	IntegerParameter rate = new IntegerParameter(this, "rate", 1);

	Output<Object> output = new Output<>(this, "output", "object");
	TimeSeriesOutput limit = new TimeSeriesOutput(this, "limitExceeded?");

	ArrayList<Long> times = null;

	public void init() {
		times = new ArrayList<>();
		this.addInput(input);
		this.addInput(time);
		this.addInput(rate);

		this.addOutput(output);
	}

	@Override
	public void sendOutput() {
		if(times.size() < rate.getValue() ||
				globals.time.getTime() - times.get(0) > time.getValue()) {
			if(times.size() >= rate.getValue()) {
				times.remove(0);
			}
			addTimeAndSend();
		} else {
			limit.send(1);
		}
	}

	private void addTimeAndSend() {
		times.add(globals.time.getTime());
		output.send(input.getValue());
		limit.send(0);
	}

	@Override
	public void clearState() {
		times = null;
	}
}
