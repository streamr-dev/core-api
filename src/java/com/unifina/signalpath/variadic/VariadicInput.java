package com.unifina.signalpath.variadic;

import com.unifina.signalpath.AbstractSignalPathModule;
import com.unifina.signalpath.Input;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Manages the lifecycle of a variable number of inputs of pre-determined type.
 * @param <T>
 */
public class VariadicInput<T> extends VariadicEndpoint<Input<T>, T> {

	public VariadicInput(AbstractSignalPathModule module, InputInstantiator<T> inputInstantiator, int defaultCount) {
		this(module, inputInstantiator, "inputs", "inputNames", defaultCount);
	}

	public VariadicInput(AbstractSignalPathModule module,
						 InputInstantiator<T> inputInstantiator,
						 String countConfig,
						 String namesConfig,
						 int defaultCount) {
		super(module, inputInstantiator, countConfig, namesConfig, defaultCount);
	}

	public List<T> getValues() {
		List<T> values = new ArrayList<>();
		for (Input<T> input : getEndpoints()) {
			values.add(input.getValue());
		}
		return values;
	}

	@Override
	public void getConfiguration(Map<String, Object> config) {
		config.put("variadicInput", true);
		super.getConfiguration(config);
	}

	@Override
	void attachToModule(AbstractSignalPathModule owner, Input<T> input) {
		owner.addInput(input);
	}

	@Override
	String getDisplayName() {
		return "in";
	}
}
