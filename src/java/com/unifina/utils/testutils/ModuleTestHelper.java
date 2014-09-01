package com.unifina.utils.testutils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.unifina.signalpath.AbstractSignalPathModule;
import com.unifina.signalpath.Input;
import com.unifina.signalpath.Output;

/**
 * Utility class for unit testing AbstractSignalPathModules.
 * Takes a list of input and output values for named inputs/outputs and checks
 * that the values produced by the module match the target values. All input
 * and output lists must be the same size. Null output values mean that no output 
 * must be produced.
 */
public class ModuleTestHelper {
	
	private List<InputHolder> inputHolders = new ArrayList<>();
	private List<OutputHolder> outputHolders = new ArrayList<>();
	private AbstractSignalPathModule module;
	int valueCount;
	
	public ModuleTestHelper(AbstractSignalPathModule module, Map<String, List<Object>> inputValuesByName, Map<String, List<Object>> outputValuesByName) {
		this.module = module;
		
		// Pair input values with actual inputs
		for (String s : inputValuesByName.keySet()) {
			Input<Object> input = module.getInput(s);
			if (input==null)
				throw new IllegalArgumentException("No input found by name: "+s);
			inputHolders.add(new InputHolder(input, inputValuesByName.get(s)));
		}

		// Pair output values with actual outputs
		for (String s : outputValuesByName.keySet()) {
			Output<Object> output = module.getOutput(s);
			if (output==null)
				throw new IllegalArgumentException("No output found by name: "+s);
			outputHolders.add(new OutputHolder(output, outputValuesByName.get(s)));
		}
		
		if (inputHolders.size()==0 || inputHolders.get(0).values.size()==0)
			throw new IllegalArgumentException("No input values given!");
		if (outputHolders.size()==0)
			throw new IllegalArgumentException("No output values given!");
		
		// Check that all input and output lists have the same number of values
		valueCount = inputHolders.get(0).values.size();
		for (InputHolder ih : inputHolders)
			if (ih.values.size()!=valueCount)
				throw new IllegalArgumentException("Input value lists are not the same size!");
		for (OutputHolder oh : outputHolders)
			if (oh.values.size()!=valueCount)
				throw new IllegalArgumentException("Output value lists are not the same size as input lists!");
		
	}
	
	/**
	 * Runs the test.
	 * @param skip Number of values to NOT test. Input values will be given and module will be activated, but output will not be tested.
	 */
	public boolean test(int skip) {
		if (skip>=valueCount)
			throw new IllegalArgumentException("All values would be skipped! Skip: "+skip+", ValueCount: "+valueCount);
		
		for (int i=0; i<valueCount; i++) {
			// Set input values
			for (InputHolder h : inputHolders)
				h.input.receive(h.values.get(i));
			
			// Activate module
			module.sendOutput();
			
			// Test outputs
			if (i>=skip) {
				for (OutputHolder h : outputHolders)  {
					Object value = h.output.getValue();
					Object target = h.values.get(i);
					
					// Possible failures:
					// - An output value exists when it should not
					// - No output value exists when it should
					// - Incorrect output value is produced
					if (target==null && value!=null || value==null && target!=null || value!=null && !value.equals(target)) {
						throwException(h, i);
					}
				}
			}
		}
		return true;
	}
	
	private void throwException(OutputHolder h, int i)  {
		Object value = h.output.getValue();
		Object target = h.values.get(i);
		
		StringBuilder sb = new StringBuilder("Incorrect value at output ")
		.append(h.output.getName())
		.append(" at index ").append(i)
		.append("! Output: ").append(value)
		.append(", Target: ").append(target)
		.append(", Inputs: [");
		
		for (InputHolder ih : inputHolders) {
			sb.append(ih.input.getName())
			.append(":")
			.append(ih.input.getValue())
			.append(", ");
		}
		sb.append("]");
		
		throw new RuntimeException(sb.toString());
	}
	
	class InputHolder {
		public Input<Object> input;
		public List<Object> values;
		
		public InputHolder(Input<Object> input, List<Object> values) {
			this.input = input;
			this.values = values;
		}
		
	}
	
	class OutputHolder {
		public Output<Object> output;
		public List<Object> values;
		
		public OutputHolder(Output<Object> output, List<Object> values) {
			this.output = output;
			this.values = values;
		}
		
	}
}
