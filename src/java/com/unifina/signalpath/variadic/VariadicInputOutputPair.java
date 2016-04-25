package com.unifina.signalpath.variadic;

import com.unifina.signalpath.AbstractSignalPathModule;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Manages the lifecycle of pairs of linked inputs and outputs. An input and output pair are always created and
 * destroyed together, as if they were a single unit.
 *
 * @param <T> the type of the inputs and outputs
 */
public class VariadicInputOutputPair<T> implements Serializable {
	private final VariadicInput<T> variadicInput;
	private final VariadicOutput<T> variadicOutput;

	public VariadicInputOutputPair(AbstractSignalPathModule module,
								   int defaultCount,
								   InputInstantiator<T> inputInstantiator,
								   OutputInstantiator<T> outputInstantiator) {
		this.variadicInput = new VariadicInput<>(module, "inputOutputPairs", defaultCount, inputInstantiator);
		this.variadicOutput = new VariadicOutput<>(module, "inputOutputPairs", defaultCount, outputInstantiator);
	}

	public void getConfiguration(Map<String,Object> config) {
		variadicInput.getConfiguration(config);
		variadicOutput.getConfiguration(config);
	}

	public void onConfiguration(Map<String,Object> config) {
		variadicInput.onConfiguration(config);
		variadicOutput.onConfiguration(config);
	}

	public void sendValuesToOutputs(List<T> values) {
		variadicOutput.send(values);
	}

	public List<T> getInputValues() {
		return variadicInput.getValues();
	}
}
