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

	public static class Builder {

		private ModuleTestHelper testHelper = new ModuleTestHelper();

		public Builder(AbstractSignalPathModule module) {
			testHelper.module = module;
		}

		public Builder(AbstractSignalPathModule module,
					   Map<String, List<Object>> inputValuesByName,
					   Map<String, List<Object>> outputValuesByName) {
			this(module);
			inputValues(inputValuesByName);
			outputValues(outputValuesByName);
		}

		public ModuleTestHelper.Builder inputValues(Map<String, List<Object>> inputValuesByName) {
			testHelper.inputValuesByName = inputValuesByName;
			return this;
		}

		public ModuleTestHelper.Builder outputValues(Map<String, List<Object>> outputValuesByName) {
			testHelper.outputValuesByName = outputValuesByName;
			return this;
		}

		public ModuleTestHelper.Builder ticks(Map<Integer, Date> ticks) {
			testHelper.turnOnTimedMode(ticks);
			return this;
		}

		public ModuleTestHelper.Builder extraIterationsAfterInput(int extraIterationsAfterInput) {
			testHelper.extraIterationsAfterInput = extraIterationsAfterInput;
			return this;
		}

		public ModuleTestHelper.Builder skip(int skip) {
			testHelper.skip = skip;
			return this;
		}

		public boolean test() throws IOException, ClassNotFoundException {
			return build().test();
		}

		public ModuleTestHelper build() {
			if (testHelper.module == null) {
				throw new RuntimeException("Field module cannot be null");
			}
			if (testHelper.inputValuesByName == null) {
				throw new RuntimeException("Field inputValuesByName cannot be null");
			}
			if (testHelper.outputValuesByName == null) {
				throw new RuntimeException("Field outputValuesByName cannot be null");
			}
			testHelper.initializeAndValidate();
			return testHelper;
		}
	}


	private AbstractSignalPathModule module;
	private Map<String, List<Object>> inputValuesByName;
	private Map<String, List<Object>> outputValuesByName;
	private Map<Integer, Date> ticks;
	private int inputValueCount;
	private int outputValueCount;
	private boolean clearStateCalled = false;
	private int extraIterationsAfterInput = 0;
	private int skip = 0;

	private ModuleTestHelper() {
	}

	private void initializeAndValidate() {
		inputValueCount = inputValuesByName.values().iterator().next().size();
		outputValueCount = (isTimedMode() ? ticks.size() : inputValueCount) + extraIterationsAfterInput - skip;
		validateThatListSizesMatch();
		falsifyNoRepeats(module);
		connectCollectorsToModule(module);
	}

	public boolean test() throws IOException, ClassNotFoundException {
		boolean a = test(false);		// Clean slate test

		clearModuleAndCollectors();
		clearStateCalled = true;
		boolean b = test(false);      // Test that clearState() works

		clearModuleAndCollectors();
		boolean c = test(true);       // Test that serialization works

		return a && b && c;
	}

	/**
	 * Runs a test.
	 */
	public boolean test(boolean withInBetweenSerializations) throws IOException, ClassNotFoundException {
		int outputIndex = 0;
		for (int i = 0; i < inputValueCount + extraIterationsAfterInput; ++i) {

			// Set input values
			if (i < inputValueCount) {
				serializeAndDeserializeModel(withInBetweenSerializations);
				feedInputs(i);
			}

			// Activate module
			serializeAndDeserializeModel(withInBetweenSerializations);
			activateModule(i);

			// Test outputs
			serializeAndDeserializeModel(withInBetweenSerializations);
			if (shouldValidateOutput(i, skip)) {
				validateOutput(withInBetweenSerializations, outputIndex, i);
				++outputIndex;
			}
		}

		return true;
	}

	private void feedInputs(int i) {
		for (Map.Entry<String, List<Object>> entry : inputValuesByName.entrySet()) {
            Input input = module.getInput(entry.getKey());
            if (input == null) {
                throw new IllegalArgumentException("No input found with name " + entry.getKey());
            }
            input.receive(entry.getValue().get(i));
        }
	}

	private void activateModule(int i) {
		module.sendOutput();
		if (isTimedMode() && ticks.containsKey(i)) {
			((ITimeListener) module).setTime(ticks.get(i));
		}
	}

	private void validateOutput(boolean withInBetweenSerializations, int outputIndex, int i) {
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
	}

	private boolean shouldValidateOutput(int i, int skip) {
		return i >= skip && (!isTimedMode() || ticks.containsKey(i));
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

	private boolean isTimedMode() {
		return ticks != null;
	}

	private void serializeAndDeserializeModel(boolean withInBetweenSerializations)
			throws IOException, ClassNotFoundException {
		if (withInBetweenSerializations) {

			// Globals is rather tricky to serialize so temporarily pull out
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
		if (skip >= inputValueCount) {
			throw new IllegalArgumentException("All values would be skipped.");
		}
		for (List<Object> inputValues : inputValuesByName.values()) {
			if (inputValues.size() != inputValueCount) {
				String msg = String.format("Input value lists are not the same size (%d).", inputValueCount);
				throw new IllegalArgumentException(msg);
			}
		}
		for (List<Object> outputValues : outputValuesByName.values()) {
			if (outputValues.size() != outputValueCount) {
				String msg = String.format("An output value list is not of expected size (%d).", outputValueCount);
				throw new IllegalArgumentException(msg);
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

	private static class Collector extends AbstractSignalPathModule {

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