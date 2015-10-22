package com.unifina.utils.testutils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.unifina.serialization.Serializer;
import com.unifina.signalpath.*;
import com.unifina.utils.DU;

/**
 * Utility class for unit testing AbstractSignalPathModules.
 * Takes a list of input and output values for named inputs/outputs and checks
 * that the values produced by the module match the target values. All input
 * and output lists must be the same size. Null output values mean that no output 
 * must be produced.
 */

/**
 * A utility class for unit testing subclasses that inherit from <code>AbstractSignalPathModule</code>.
 *
 * Given an instance of the subclass, and a list of input values and expected output values, this test verifies that the
 * inherited class fulfils its contract, in other words, it
 *
 * 	- produces output values that match with expected output
 * 	- implements clearState() properly
 * 	- properly serializes and deserializes without affecting results
 *
 */
public class ModuleTestHelper {

	private AbstractSignalPathModule module;
	private Map<String, List<Object>> inputValuesByName;
	private Map<String, List<Object>> outputValuesByName;
	private int valueCount;
	private boolean clearStateCalled = false;
	
	public ModuleTestHelper(AbstractSignalPathModule module,
							Map<String, List<Object>> inputValuesByName,
							Map<String, List<Object>> outputValuesByName) {

		this.module = module;
		this.inputValuesByName = inputValuesByName;
		this.outputValuesByName = outputValuesByName;
		this.valueCount = inputValuesByName.values().iterator().next().size();

		validateThatInputAndOutputListsAreAllOfSameSize();
		falsifyNoRepeats(module);
		connectCollectorsToModule(module);
	}

	public boolean test() throws IOException, ClassNotFoundException {
		return test(0);
	}

	public boolean test(int skip) throws IOException, ClassNotFoundException {
		boolean a = test(skip, false);		// Clean slate test

		clearModuleAndCollectors();
		clearStateCalled = true;
		boolean b = test(skip, false);      // Test that clearState() works

		clearModuleAndCollectors();
		boolean c = test(skip, true);       // Test that serialization works

		return a && b && c;
	}

	/**
	 * Runs a test.
	 * @param skip Number of values to NOT test. Input values will be given and module will be activated, but output will not be tested.
	 */
	public boolean test(int skip, boolean withInBetweenSerializations) throws IOException, ClassNotFoundException {
		if (skip>=valueCount) {
			throw new IllegalArgumentException("All values would be skipped.");
		}

		for (int i = 0; i < valueCount; ++i) {

			serializeAndDeserializeModel(withInBetweenSerializations);

			// Set input values
			for (Map.Entry<String, List<Object>> entry : inputValuesByName.entrySet()) {
				Input input = module.getInput(entry.getKey());
				if (input == null) {
					throw new IllegalArgumentException("No input found with name " + entry.getKey());
				}
				input.receive(entry.getValue().get(i));
			}

			serializeAndDeserializeModel(withInBetweenSerializations);

			// Activate module
			module.sendOutput();

			serializeAndDeserializeModel(withInBetweenSerializations);

			// Test outputs
			if (i >= skip) {
				for (Map.Entry<String, List<Object>> entry : outputValuesByName.entrySet()) {

					Object actual = module.getOutput(entry.getKey()).getTargets()[0].getValue();
					Object expected = entry.getValue().get(i);

					if (actual instanceof Double) {
						actual = DU.clean((Double) actual);
					}

					// Possible failures:
					// - An output value exists when it should not
					// - No output value exists when it should
					// - Incorrect output value is produced
					if ((expected == null && actual != null) ||
							(expected != null && actual == null) ||
							expected != null && !expected.equals(actual)) {

						throwException(entry.getKey(), i, withInBetweenSerializations, actual, expected);
					}
				}
			}
		}
		return true;
	}

	private void throwException(String outputName, int i, boolean withInBetweenSerializations, Object value,
								Object target)  {

		StringBuilder sb = new StringBuilder("Incorrect value at output ")
				.append(outputName)
				.append(" at index ").append(i)
				.append(withInBetweenSerializations ? " with" : " without").append(" serialization, ")
				.append("clearState() called? ").append(clearStateCalled)
				.append("! Output: ").append(value)
				.append(", Target: ").append(target)
				.append(", Inputs: [");

		for (Map.Entry<String, List<Object>> entry : inputValuesByName.entrySet()) {
			sb.append(entry.getKey())
					.append(":")
					.append(entry.getValue())
					.append(", ");
		}
		sb.append("]");

		throw new RuntimeException(sb.toString());
	}

	private void serializeAndDeserializeModel(boolean withInBetweenSerializations)
			throws IOException, ClassNotFoundException {
		if (withInBetweenSerializations) {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			Serializer.serialize(module, out);
			ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
			module = (AbstractSignalPathModule) Serializer.deserialize(in);
		}
	}

	private void clearModuleAndCollectors() {
		module.clearState();
		for (Output<Object> output : module.getOutputs()) {
			for (Input<Object> target : output.getTargets()) {
				target.receive(null); // TODO: this shouldn't be needed.
				target.getOwner().clear();
			}
		}
	}

	private void validateThatInputAndOutputListsAreAllOfSameSize() {
		for (List<Object> inputValues : inputValuesByName.values()) {
			if (inputValues.size() != valueCount) {
				throw new IllegalArgumentException("Input value lists are not the same size.");
			}
		}
		for (List<Object> outputValues : outputValuesByName.values()) {
			if (outputValues.size() != valueCount) {
				throw new IllegalArgumentException("An output value list is not the same size as input lists.");
			}
		}
	}

	private static void falsifyNoRepeats(AbstractSignalPathModule module) {
		for (Output output : module.getOutputs()) {
			if (output instanceof TimeSeriesOutput) {
				((TimeSeriesOutput) output).noRepeat = false;
			}
		}
	}

	private void connectCollectorsToModule(AbstractSignalPathModule module) {
		for (String outputName : outputValuesByName.keySet()) {
			Output output = module.getOutput(outputName);
			if (output == null) {
				throw new IllegalArgumentException("No output found with name " + outputName);
			}
			Collector collector = new Collector();
			collector.init();
			collector.attachToModule(output);
		}
	}

	static class Collector extends AbstractSignalPathModule {

		Input<Object> input = new Input<>(this, "input", "Object");

		@Override
		public void init() {
			addInput(input);
		}

		@Override
		public void sendOutput() {
		}

		@Override
		public void clearState() {
		}

		public void attachToModule(Output<Object> externalOutput) {
			externalOutput.connect(input);
		}
	}
}