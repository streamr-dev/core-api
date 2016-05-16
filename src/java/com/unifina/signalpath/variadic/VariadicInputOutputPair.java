package com.unifina.signalpath.variadic;

import com.unifina.signalpath.AbstractSignalPathModule;
import com.unifina.signalpath.Input;
import com.unifina.signalpath.Output;

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
								   InputInstantiator<T> inputInstantiator,
								   OutputInstantiator<T> outputInstantiator) {
		this(module, inputInstantiator, outputInstantiator, 1);
	}

	public VariadicInputOutputPair(AbstractSignalPathModule module,
								   InputInstantiator<T> inputInstantiator,
								   OutputInstantiator<T> outputInstantiator,
								   int startIndex) {
		variadicInput = new VariadicInput<>(module, inputInstantiator, startIndex);
		variadicOutput = new VariadicOutput<>(module, outputInstantiator, startIndex);
	}

	public void onConfiguration(Map<String, Object> config) {
		variadicInput.onConfiguration(config);
		variadicOutput.onConfiguration(config);
		linkInputsToOutputs(variadicInput.getEndpointsIncludingPlaceholder(),
			variadicOutput.getEndpointsIncludingPlaceholder());
	}

	public void sendValuesToOutputs(List<T> values) {
		variadicOutput.send(values);
	}

	public List<T> getInputValues() {
		return variadicInput.getValues();
	}

	public Input addInput(String name) {
		return variadicInput.addEndpoint(name);
	}

	public Output addOutput(String name) {
		return variadicOutput.addEndpoint(name);
	}

	private void linkInputsToOutputs(List<Input<T>> inputs, List<Output<T>> outputs) {
		for (int i=0; i < inputs.size(); ++i) {
			Input<T> input = inputs.get(i);
			Output<T> output = outputs.get(i);

			Map<String, Object> inputConfig = input.getConfiguration();
			Map<String, Object> variadicInputConfig = (Map<String, Object>) inputConfig.get("variadic");
			variadicInputConfig.put("linkedOutput", output.getName());

			Map<String, Object> outputConfig = output.getConfiguration();
			Map<String, Object> variadicOutputConfig = (Map<String, Object>) outputConfig.get("variadic");
			variadicOutputConfig.put("disableGrow", true);
		}
	}
}
