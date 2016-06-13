package com.unifina.signalpath.simplemath;

import com.unifina.signalpath.*;
import com.unifina.signalpath.variadic.VariadicInput;
import com.unifina.signalpath.variadic.InputInstantiator;

import java.util.Map;

public class VariadicAddMulti extends AbstractSignalPathModule {

	private TimeSeriesInput in1 = new TimeSeriesInput(this, "in1");
	private TimeSeriesInput in2 = new TimeSeriesInput(this, "in2");
	private VariadicInput<Double> variadicInput = new VariadicInput<>(this, new InputInstantiator.TimeSeries(), 3);
	private TimeSeriesOutput out = new TimeSeriesOutput(this, "sum");

	@Override
	public void init() {
		addInput(in1);
		addInput(in2);
		addOutput(out);
	}

	public void clearState() {}

	public void sendOutput() {
		double sum = 0;
		sum += in1.getValue();
		sum += in2.getValue();
		for (Double val : variadicInput.getValues()) {
			sum += val;
		}
		out.send(sum);
	}

	@Override
	public Input getInput(String name) {
		Input input = super.getInput(name);
		if (input == null) {
			input = variadicInput.addEndpoint(name);
		}
		return input;
	}

	@Override
	protected void onConfiguration(Map<String, Object> config) {
		super.onConfiguration(config);
		variadicInput.onConfiguration(config);
	}
}
