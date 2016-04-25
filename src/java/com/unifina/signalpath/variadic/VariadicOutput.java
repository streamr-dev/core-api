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
	private final OutputInstantiator<T> outputInstantiator;

	public VariadicOutput(AbstractSignalPathModule module, int defaultCount, OutputInstantiator<T> outputInstantiator) {
		this(module, "outputs", defaultCount, outputInstantiator);
	}

	public VariadicOutput(AbstractSignalPathModule module,
						  String configOption,
						  int defaultCount,
						  OutputInstantiator<T> outputInstantiator) {
		super(module, configOption, defaultCount);
		this.outputInstantiator = outputInstantiator;
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
	Output<T> makeAndAttachNewEndpoint(AbstractSignalPathModule owner, int index) {
		Output<T> output = outputInstantiator.instantiate(owner, index);
		owner.addOutput(output);
		return output;
	}
}
