package com.unifina.signalpath.variadic;

import com.unifina.signalpath.AbstractSignalPathModule;
import com.unifina.signalpath.Output;

import java.util.List;
import java.util.Map;

/**
 * Manages the lifecycle of a variable number of outputs of pre-determined type.
 * @param <T>
 */
public class VariadicOutput<T> extends VariadicEndpoint<Output<T>, T> {

	public VariadicOutput(AbstractSignalPathModule module, OutputInstantiator<T> outputInstantiator) {
		super(module, outputInstantiator, "outputNames");
	}

	public VariadicOutput(AbstractSignalPathModule module, OutputInstantiator<T> outputInstantiator, int startIndex) {
		super(module, outputInstantiator, "outputNames", startIndex);
	}

	public void send(List<T> values) {
		if (values.size() != getEndpoints().size()) {
			throw new IllegalArgumentException("Size of argument list does not match number of outputs");
		}
		for (int i=0; i < values.size(); ++i) {
			T value = values.get(i);
			if (value != null) {
				getEndpoints().get(i).send(value);
			}
		}
	}

	@Override
	public void getConfiguration(Map<String, Object> config) {
		config.put("variadicOutput", true);
		super.getConfiguration(config);
	}

	@Override
	void attachToModule(AbstractSignalPathModule owner, Output<T> output) {
		owner.addOutput(output);
	}

	@Override
	void handlePlaceholder(Output<T> placeholderOutput) {}

	@Override
	String getDisplayName() {
		return "out";
	}
}
