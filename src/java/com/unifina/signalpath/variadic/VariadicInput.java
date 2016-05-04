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

	public VariadicInput(AbstractSignalPathModule module, InputInstantiator<T> inputInstantiator) {
		super(module, inputInstantiator, "inputNames");
	}

	public VariadicInput(AbstractSignalPathModule module, InputInstantiator<T> inputInstantiator, int startIndex) {
		super(module, inputInstantiator, "inputNames", startIndex);
	}

	public List<T> getValues() {
		List<T> values = new ArrayList<>();
		for (Input<T> input : getEndpoints()) {
			values.add(input.getValue());
		}
		return values;
	}

	@Override
	void attachToModule(AbstractSignalPathModule owner, Input<T> input) {
		owner.addInput(input);
	}

	@Override
	void handlePlaceholder(Input<T> placeholderInput) {
		placeholderInput.requiresConnection = false;
	}

	@Override
	String getDisplayName() {
		return "in";
	}

	@Override
	String getModuleConfigField() {
		return "inputs";
	}
}
