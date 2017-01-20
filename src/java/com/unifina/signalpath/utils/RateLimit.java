package com.unifina.signalpath.utils;

import com.unifina.signalpath.*;

import java.util.ArrayList;
import java.util.*;

public class RateLimit extends AbstractSignalPathModule {

	Input<Object> in = new Input<>(this, "in", "Object");

	IntegerParameter time = new IntegerParameter(this, "timeInMillis", 1000);
	IntegerParameter rate = new IntegerParameter(this, "rate", 1);

	Output<Object> out = new Output<>(this, "out", "Object");
	TimeSeriesOutput limit = new TimeSeriesOutput(this, "limitExceeded?");

	List<Number> times = new ArrayList<>();

	@Override
	public void sendOutput() {
		if (time.getValue() != 0) {
			times.add(getGlobals().time.getTime());
			while (getGlobals().time.getTime() - times.get(0).longValue() >= time.getValue().longValue()) {
				times.remove(0);
			}
		}
		if (times.size() <= rate.getValue()) {
			out.send(in.getValue());
			limit.send(0);
		} else {
			times.remove(times.size()-1);
			limit.send(1);
		}
	}

	@Override
	public void clearState() {
		times = new ArrayList<>();
	}
}
