package com.unifina.signalpath.random;

import com.unifina.signalpath.DoubleParameter;
import com.unifina.signalpath.Input;
import com.unifina.signalpath.TimeSeriesOutput;

/**
 * Generates random numbers from a Gaussian distribution
 */
public class RandomNumberGaussian extends ModuleWithRandomness {
	private final DoubleParameter mean = new DoubleParameter(this, "mean", 0.0);
	private final DoubleParameter sd = new DoubleParameter(this, "sd", 1.0);
	private final Input<Object> trigger = new Input<>(this, "trigger", "Object");
	private final TimeSeriesOutput out = new TimeSeriesOutput(this, "out");


	@Override
	public void init() {
		addInput(mean);
		addInput(sd);
		addInput(trigger);
		addOutput(out);
	}

	@Override
	public void sendOutput() {
		if (sd.getValue() > 0) {
			out.send(getRandom().nextGaussian() * sd.getValue() + mean.getValue());
		}
		// TODO: else signal warning to debug console
	}
}
