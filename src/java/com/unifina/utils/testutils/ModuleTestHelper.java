package com.unifina.utils.testutils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

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

		public Builder inputValues(Map<String, List<Object>> inputValuesByName) {
			testHelper.inputValuesByName = inputValuesByName;
			return this;
		}

		public Builder outputValues(Map<String, List<Object>> outputValuesByName) {
			testHelper.outputValuesByName = outputValuesByName;
			return this;
		}

		public Builder uiChannelMessages(Map<String, List<Object>> uiChannelMessages) {
			testHelper.turnOnUiChannelMode(uiChannelMessages);
			return this;
		}

		public Builder ticks(Map<Integer, Date> ticks) {
			testHelper.turnOnTimedMode(ticks);
			return this;
		}

		public Builder extraIterationsAfterInput(int extraIterationsAfterInput) {
			testHelper.extraIterationsAfterInput = extraIterationsAfterInput;
			return this;
		}

		public Builder skip(int skip) {
			testHelper.skip = skip;
			return this;
		}

		public Builder timeToFurtherPerIteration(int timeStep) {
			testHelper.timeStep = timeStep;
			return this;
		}


		public Builder sendNullInputs(boolean sendNullInputs) {
			testHelper.sendNullInputs = sendNullInputs;
			return this;
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

		public boolean test() throws IOException, ClassNotFoundException {
			return build().test();
		}
	}


	private AbstractSignalPathModule module;
	private Map<String, List<Object>> inputValuesByName;
	private Map<String, List<Object>> outputValuesByName;
	private Map<String, List<Object>> uiChannelMessages;
	private Map<Integer, Date> ticks;
	private int extraIterationsAfterInput = 0;
	private int skip = 0;
	private int timeStep = 0;
	private boolean sendNullInputs = true;

	private int inputValueCount;
	private int outputValueCount;
	private boolean clearStateCalled = false;

	private ModuleTestHelper() {}

	public boolean test() throws IOException, ClassNotFoundException {
		boolean a = test(false);		// Clean slate test

		clearModuleAndCollectorsAndChannels();
		clearStateCalled = true;
		boolean b = test(false);      // Test that clearState() works

		clearModuleAndCollectorsAndChannels();
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
			} else {
				module.setSendPending(true); // TODO: ugly hack, isn't concern of user of module!
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

			// Further global time
			furtherTime();
		}

		// Test ui channel messages
		if (isUiChannelMode()) {
			validateUiChannelMessages(withInBetweenSerializations);
		}

		return true;
	}

	private void feedInputs(int i) {
		for (Map.Entry<String, List<Object>> entry : inputValuesByName.entrySet()) {
			Input input = module.getInput(entry.getKey());
			if (input == null) {
				throw new IllegalArgumentException("No input found with name " + entry.getKey());
			}
			Object val = entry.getValue().get(i);
			if (val != null || sendNullInputs) {
				input.receive(val);
			}
		}
	}

	private void activateModule(int i) {
		module.trySendOutput();
		if (isTimedMode() && ticks.containsKey(i)) {
			((ITimeListener) module).setTime(ticks.get(i));
		}
	}

	private boolean shouldValidateOutput(int i, int skip) {
		return i >= skip && (!isTimedMode() || ticks.containsKey(i));
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

	private void throwException(String outputName, int i, int outputIndex,
								boolean withInBetweenSerializations, Object value, Object target)  {
		String msg = "%s: mismatch at %d (inputIndex %d), was '%s' expected '%s'" +
				moduleStateAsString(withInBetweenSerializations);
		throw new RuntimeException(String.format(msg, outputName, outputIndex, i, value, target));
	}

	private void furtherTime() {
		module.globals.time = new Date(module.globals.time.getTime() + timeStep);
	}

	private void validateUiChannelMessages(boolean withInBetweenSerializations) {

		String moduleState = moduleStateAsString(withInBetweenSerializations);

		FakePushChannel uiChannel = (FakePushChannel) module.globals.getUiChannel();
		if (uiChannel == null) {
			throw new RuntimeException("uiChannel: module.globals.uiChannel unexpectedly null" + moduleState);
		}

		for (Map.Entry<String, List<Object>> expectedEntry : uiChannelMessages.entrySet()) {
			String channel = expectedEntry.getKey();
			List<Object> expectedMessages = expectedEntry.getValue();

			if (!uiChannel.receivedContentByChannel.containsKey(channel)) {
				throw new RuntimeException(String.format("uiChannel: channel '%s' was never pushed to" + moduleState,
						channel));
			}

			List<Object> actualMessages = uiChannel.receivedContentByChannel.get(channel);
			for (int i = 0; i < Math.max(expectedMessages.size(), actualMessages.size()); ++i) {

				if (actualMessages.size() <= i) {
					String msg = "uiChannel: expected %d messages on channel '%s' but received only %d";
					throw new RuntimeException(String.format(msg + moduleState,
							expectedMessages.size(),
							channel,
							actualMessages.size()
					));
				}

				if (expectedMessages.size() <= i) {
					String msg = "uiChannel: expected %d messages on channel '%s' but received %d additional: %s";
					throw new RuntimeException(String.format(msg + moduleState,
							expectedMessages.size(),
							channel,
							actualMessages.size() - expectedMessages.size(),
							Arrays.toString(actualMessages.subList(i, actualMessages.size()).toArray())
					));
				}

				Object actual = actualMessages.get(i);
				Object expected = expectedMessages.get(i);

				if (!actual.equals(expected)) {
					String msg = "uiChannel: mismatch at %d, was '%s' expected '%s'" + moduleState;
					throw new RuntimeException(String.format(msg, i, actual, expected));
				}
			}
		}
	}

	private String moduleStateAsString(boolean withInBetweenSerializations) {
		return " (clearState=" + clearStateCalled + ", serialized=" + withInBetweenSerializations + ")";
	}

	private boolean isUiChannelMode() {
		return uiChannelMessages != null;
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

	private void clearModuleAndCollectorsAndChannels() {
		setUpGlobals(module);
		module.clearState();
		for (Output<Object> output : module.getOutputs()) {
			for (Input<Object> target : output.getTargets()) {
				target.receive(null); // TODO: this shouldn't be needed.
				target.getOwner().clear();
			}
		}
		module.connectionsReady();
	}

	private void turnOnUiChannelMode(Map<String, List<Object>> uiChannelMessages) {
		if (module instanceof ModuleWithUI) {
			this.uiChannelMessages = new HashMap<>(uiChannelMessages);
		} else {
			throw new RuntimeException("Module does not extend ModuleWithUI");
		}
	}


	private void turnOnTimedMode(Map<Integer, Date> ticks) {
		if (module instanceof ITimeListener) {
			this.ticks = new HashMap<>(ticks);
		} else {
			throw new RuntimeException("Module does not implement ITimeListener interface");
		}
	}


	// Initialization steps below

	private void initializeAndValidate() {
		inputValueCount = inputValuesByName.isEmpty() ? 0 : inputValuesByName.values().iterator().next().size();
		outputValueCount = (isTimedMode() ? ticks.size() : inputValueCount) + extraIterationsAfterInput - skip;
		validateThatListSizesMatch();
		falsifyNoRepeats(module);
		connectCollectorsToModule(module);
		setUpGlobals(module);
		module.connectionsReady();
	}

	private void validateThatListSizesMatch() {
		if (skip >= inputValueCount && extraIterationsAfterInput == 0) {
			throw new IllegalArgumentException("All values would be skipped and not a single output tested.");
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

	private static void setUpGlobals(AbstractSignalPathModule module) {
		module.globals = new Globals();
		module.globals.time = new Date(0);
		module.globals.setUiChannel(new FakePushChannel());
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