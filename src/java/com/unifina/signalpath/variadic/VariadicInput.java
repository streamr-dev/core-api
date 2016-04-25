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
	private final InputInstantiator<T> inputInstantiator;

	public VariadicInput(AbstractSignalPathModule module, int defaultCount, InputInstantiator<T> inputInstantiator) {
		this(module, "inputs", defaultCount, inputInstantiator);
	}

	public VariadicInput(AbstractSignalPathModule module,
						 String configOption,
						 int defaultCount,
						 InputInstantiator<T> inputInstantiator) {
		super(module, configOption, defaultCount);
		this.inputInstantiator = inputInstantiator;
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
	Input<T> makeAndAttachNewEndpoint(AbstractSignalPathModule owner, int index) {
		Input<T> input = inputInstantiator.instantiate(owner, index);
		owner.addInput(input);
		return input;
	}
}
