package com.unifina.utils.testutils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.unifina.datasource.ITimeListener;
import com.unifina.serialization.Serializer;
import com.unifina.signalpath.*;
import com.unifina.utils.DU;
import com.unifina.utils.Globals;


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
	private Map<Integer, Date> ticks;
	private int inputValueCount;
	private boolean clearStateCalled = false;
	
	public ModuleTestHelper(AbstractSignalPathModule module,
							Map<String, List<Object>> inputValuesByName,
							Map<String, List<Object>> outputValuesByName) {

		this(module, inputValuesByName, outputValuesByName, null);
	}

	public ModuleTestHelper(AbstractSignalPathModule module,
							Map<String, List<Object>> inputValuesByName,
							Map<String, List<Object>> outputValuesByName,
							Map<Integer, Date> ticks) {

		this.module = module;
		this.inputValuesByName = inputValuesByName;
		this.outputValuesByName = outputValuesByName;
		this.inputValueCount = inputValuesByName.values().iterator().next().size();

		if (ticks != null) {
			turnOnTimedMode(ticks);
		}

		validateThatListSizesMatch();
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
		if (skip >= inputValueCount) {
			throw new IllegalArgumentException("All values would be skipped.");
		}

		int outputIndex = 0;
		for (int i = 0; i < inputValueCount; ++i) {

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
			invokeSetTimeIfNecessary(i);

			serializeAndDeserializeModel(withInBetweenSerializations);

			// Test outputs
			if (shouldValidateOutput(i, skip)) {
				for (Map.Entry<String, List<Object>> entry : outputValuesByName.entrySet()) {

					Object actual = module.getOutput(entry.getKey()).getTargets()[0].getValue();
					Object expected = entry.getValue().get(outputIndex);

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

						throwException(entry.getKey(), i, outputIndex, withInBetweenSerializations, actual, expected);
					}
				}
				++outputIndex;
			}
		}
		return true;
	}

	private void invokeSetTimeIfNecessary(int i) {
		if (isTimedMode() && ticks.containsKey(i)) {
			((ITimeListener) module).setTime(ticks.get(i));
		}
	}

	private boolean shouldValidateOutput(int i, int skip) {
		return i >= skip && (!isTimedMode() || ticks.containsKey(i));
	}

	private boolean isTimedMode() {
		return ticks != null;
	}

	private void throwException(String outputName, int i, int outputIndex,
								boolean withInBetweenSerializations, Object value, Object target)  {

		StringBuilder sb = new StringBuilder("Incorrect value at output '")
				.append(outputName).append("'")
				.append(" at index ").append(outputIndex)
				.append(" (input index ").append(i).append(")")
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

			// Globals is rather tricky to serialize
			Globals globalsTempHolder = module.globals;
			module.globals = null;

			ByteArrayOutputStream out = new ByteArrayOutputStream();
			Serializer.serialize(module, out);

			//Serializer.serializeToFile(module, "temp.out");

			ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
			module = (AbstractSignalPathModule) Serializer.deserialize(in);
			module.globals = globalsTempHolder;
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

	private void turnOnTimedMode(Map<Integer, Date> ticks) {
		if (module instanceof ITimeListener) {
			this.ticks = new HashMap<>(ticks);
		} else {
			throw new RuntimeException("Module does not implement ITimeListener interface");
		}
	}

	private void validateThatListSizesMatch() {
		for (List<Object> inputValues : inputValuesByName.values()) {
			if (inputValues.size() != inputValueCount) {
				throw new IllegalArgumentException("Input value lists are not the same size.");
			}
		}

		int outputCount = isTimedMode() ? ticks.size() : inputValueCount;

		for (List<Object> outputValues : outputValuesByName.values()) {
			if (outputValues.size() != outputCount) {
				throw new IllegalArgumentException("An output value list is not of expected size.");
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