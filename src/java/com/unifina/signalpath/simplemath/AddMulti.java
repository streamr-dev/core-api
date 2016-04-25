package com.unifina.signalpath.simplemath;

import com.unifina.signalpath.*;
import com.unifina.signalpath.variadic.VariadicInput;
import com.unifina.signalpath.variadic.InputInstantiator;

import java.util.Map;

public class AddMulti extends AbstractSignalPathModule {

	private VariadicInput<Double> variadicInput = new VariadicInput<>(this, 2, new InputInstantiator.TimeSeries());
	private TimeSeriesOutput out = new TimeSeriesOutput(this, "sum");

	@Override
	public void init() {
		addOutput(out);
	}

	public void clearState() {}

	public void sendOutput() {
		double sum = 0;
		for (Double val : variadicInput.getValues()) {
			sum += val;
		}
		out.send(sum);
	}

	@Override
	public Map<String, Object> getConfiguration() {
		Map<String, Object> config = super.getConfiguration();
		variadicInput.getConfiguration(config);
		return config;
	}

	@Override
	public void onConfiguration(Map<String, Object> config) {
		super.onConfiguration(config);
		variadicInput.onConfiguration(config);
	}
}
