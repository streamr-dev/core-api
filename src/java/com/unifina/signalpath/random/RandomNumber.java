package com.unifina.signalpath.random;

import com.unifina.signalpath.*;

/**
 * Generates random numbers from uniform distribution
 */
public class RandomNumber extends ModuleWithRandomness {
	private final DoubleParameter min = new DoubleParameter(this, "min", -1.0);
	private final DoubleParameter max = new DoubleParameter(this, "max", 1.0);
	private final Input<Object> trigger = new Input<>(this, "trigger", "Object");
	private final TimeSeriesOutput out = new TimeSeriesOutput(this, "out");

	@Override
	public void init() {
		addInput(min);
		addInput(max);
		addInput(trigger);
		addOutput(out);
	}



	@Override
	public void sendOutput() {
		double lowerBound = min.getValue();
		double upperBound = max.getValue();

		double middle = (lowerBound + upperBound) / 2;
		double intervalLength = upperBound - lowerBound;

		if (intervalLength > 0) {
			out.send((getRandom().nextDouble() - .5) * intervalLength + middle);
		}
		// TODO: else signal warning to debug console
	}
}
