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
	private final AbstractSignalPathModule module;
	private final VariadicInput<T> variadicInput;
	private final VariadicOutput<T> variadicOutput;

	public VariadicInputOutputPair(String inputBaseName,
								   String outputBaseName,
								   AbstractSignalPathModule module,
								   InputInstantiator<T> inputInstantiator,
								   OutputInstantiator<T> outputInstantiator) {
		this(inputBaseName, outputBaseName, module, inputInstantiator, outputInstantiator, 1);
	}

	public VariadicInputOutputPair(String inputBaseName,
								   final String outputBaseName,
								   AbstractSignalPathModule module,
								   InputInstantiator<T> inputInstantiator,
								   OutputInstantiator<T> outputInstantiator,
								   int startIndex) {
		variadicInput = new VariadicInput<T>(inputBaseName, module, inputInstantiator, startIndex) {
			@Override
			public Map<String, Object> getConfiguration() {
				Map<String, Object> config = super.getConfiguration();
				config.put("linkedTo", outputBaseName);
				return config;
			}
		};
		variadicOutput = new VariadicOutput<T>(outputBaseName, module, outputInstantiator, startIndex) {
			@Override
			public Map<String, Object> getConfiguration() {
				Map<String, Object> config = super.getConfiguration();
				config.put("disableGrowOnConnection", true);
				return config;
			}
		};
		this.module = module;
	}

	public void init() {
		module.addVariadic(variadicInput);
		module.addVariadic(variadicOutput);
	}

	public void sendValuesToOutputs(List<T> values) {
		variadicOutput.send(values);
	}

	public List<T> getInputValues() {
		return variadicInput.getValues();
	}
}
