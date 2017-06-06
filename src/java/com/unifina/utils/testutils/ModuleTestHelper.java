package com.unifina.utils.testutils;

import com.unifina.datasource.ITimeListener;
import com.unifina.serialization.Serializer;
import com.unifina.serialization.SerializerImpl;
import com.unifina.service.SerializationService;
import com.unifina.signalpath.*;
import com.unifina.utils.DU;
import com.unifina.utils.Globals;
import groovy.lang.Closure;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;


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

		/**
		 * Set input values grouped by input name (mandatory)
		 */
		public Builder inputValues(Map<String, List<Object>> inputValuesByName) {
			testHelper.inputValuesByName = inputValuesByName;
			return this;
		}

		/**
		 * Set expected output values grouped by output name (mandatory)
		 */
		public Builder outputValues(Map<String, List<Object>> outputValuesByName) {
			testHelper.outputValuesByName = outputValuesByName;
			return this;
		}

		/**
		 * Set expected messages that should've been pushed to ui channel at end of input list. (optional)
		 * Notice that previously set <code>module</code> must be an instance of <code>ModuleWithUI</code>.
		 */
		public Builder uiChannelMessages(Map<String, List<Map>> expectedUiChannelMessages, Map<String, List<Map>> sentUiChannelMessages) {
			testHelper.turnOnUiChannelMode(expectedUiChannelMessages, sentUiChannelMessages);
			return this;
		}

		/**
		 * Command the helper to invoke <code>setTime(Date)</code> at given iterations with given Date value.
		 * Notice that previously set <code>module</code> must be an instance of <code>ITimeListener</code>.
		 */
		public Builder ticks(Map<Integer, Date> ticks) {
			testHelper.turnOnTimedMode(ticks);
			return this;
		}

		/**
		 * After input list has been iterated through, run additional rounds during which <code>trySendOutput()</code>
		 * (and possibly <code>setTime(Date)</code>) is invoked, and output is verified.
		 */
		public Builder extraIterationsAfterInput(int extraIterationsAfterInput) {
			testHelper.extraIterationsAfterInput = extraIterationsAfterInput;
			return this;
		}

		/**
		 * Determine a certain number of rounds from the very beginning during which input is fed but output is not
		 * verified.
		 */
		public Builder skip(int skip) {
			testHelper.skip = skip;
			return this;
		}

		/**
		 * How many milliseconds to further <code>globals.time</code> after each round. (default = 0)
		 */
		public Builder timeToFurtherPerIteration(int timeStep) {
			testHelper.timeSteps = Arrays.asList(timeStep);
			return this;
		}

		/**
		 * How many milliseconds to further <code>globals.time</code> after each round by round.
		 */
		public Builder timeToFurtherPerIteration(List<Integer> timeSteps) {
			testHelper.timeSteps = timeSteps;
			return this;
		}

		/**
		 * Whether null values in input lists should be fed to to inputs (default = false, null values are ignored)
		 */
		public Builder feedNullInputs(boolean feedNullInputs) {
			testHelper.feedNullInputs = feedNullInputs;
			return this;
		}

		/**
		 * Override or extend the module's <code>globals</code> instance. Return value is interpreted as new
		 * <code>globals</code>.
		 */
		public Builder overrideGlobals(Closure overrideGlobalsClosure) {
			testHelper.overrideGlobalsClosure = overrideGlobalsClosure;
			return this;
		}

		/**
		 * Additional operations after a test case has been finished. Return value is ignored.
		 */
		public Builder afterEachTestCase(Closure<?> afterEachTestCase) {
			testHelper.afterEachTestCase = afterEachTestCase;
			return this;
		}

		/**
		 * Additional operations before a test case has been run. <code>module</code> is passed as argument to closure.
		 * Return value is ignored.
		 */
		public Builder beforeEachTestCase(Closure<?> beforeEachTestCase) {
			testHelper.beforeEachTestCase = beforeEachTestCase;
			return this;
		}

		/**
		 * Serialization+deserialization changes module instance, and old references go stale.
		 * Test Specification must listen to instance changes and update their references
		 *   if they make direct calls to the module in the middle of the test (e.g. in Stub'd methods)
         */
		public Builder onModuleInstanceChange(Closure<?> moduleInstanceChanged) {
			testHelper.moduleInstanceChanged = moduleInstanceChanged;
			return this;
		}

		/**
		 * By default, test all SerializationModes (NONE, SERIALIZE and SERIALIZE_DESERIALIZE).
		 * This can be changed by calling this method with a subset of those SerializationModes.
         */
		public Builder serializationModes(Set<SerializationMode> serializationModes) {
			testHelper.selectedSerializationModes = serializationModes;
			return this;
		}

		/**
		 * Build the <code>ModuleTestHelper</code>. Throws <code>RuntimeException</code> if test setting has been
		 * configured inappropriately.
		 */
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
			verifyNoTicksBeforeSkip();
			testHelper.initializeAndValidate();
			return testHelper;
		}

		/**
		 * Shortcut for building the <code>ModuleTestHelper</code> and running it.
		 */
		public boolean test() throws IOException, ClassNotFoundException {
			return build().test();
		}

		private void verifyNoTicksBeforeSkip() {
			if (testHelper.ticks != null) {
				for (Integer tickAt : testHelper.ticks.keySet()) {
					if (tickAt < testHelper.skip) {
						String msg = "A tick (at %d) occurred before skip %d.";
						throw new RuntimeException(String.format(msg, tickAt, testHelper.skip));
					}
				}
			}
		}
	}


	private AbstractSignalPathModule module;
	private Map<String, List<Object>> inputValuesByName;
	private Map<String, List<Object>> outputValuesByName;
	private Map<String, List<Map>> expectedUiChannelMessages;
	private Map<String, List<Map>> sentUiChannelMessages;
	private Map<Integer, Date> ticks;
	private int extraIterationsAfterInput = 0;
	private int skip = 0;
	private List<Integer> timeSteps = Arrays.asList(0);
	private boolean feedNullInputs = false;
	private Closure<Globals> overrideGlobalsClosure = Closure.IDENTITY;
	private Closure<?> beforeEachTestCase = Closure.IDENTITY;
	private Closure<?> afterEachTestCase = Closure.IDENTITY;
	private Closure<?> moduleInstanceChanged = Closure.IDENTITY;

	private int inputValueCount;
	private int outputValueCount;
	private boolean clearStateCalled = false;

	public enum SerializationMode {
		NONE, SERIALIZE, SERIALIZE_DESERIALIZE
	}
	private SerializationMode serializationMode = SerializationMode.NONE;
	private SerializationService dummySerializationService = new SerializationService();

	private Serializer serializer = new SerializerImpl();

	private ModuleTestHelper() {}

	// By default, test all serialization modes
	private Set<SerializationMode> selectedSerializationModes = new HashSet<>(Arrays.asList(SerializationMode.values()));

	public boolean test() throws IOException, ClassNotFoundException {
		try {
			boolean pass = true;

			if (selectedSerializationModes.contains(SerializationMode.NONE)) {
				if (!runTestCase()) {        // Clean slate test
					pass = false;
				}

				clearModuleAndCollectorsAndChannels();
				clearStateCalled = true;
				if (!runTestCase()) {        // Test that clearState() works
					pass = false;
				}
			}

			if (selectedSerializationModes.contains(SerializationMode.SERIALIZE)) {
				clearModuleAndCollectorsAndChannels();
				serializationMode = SerializationMode.SERIALIZE;
				if (!runTestCase()) {       // Test that serialization works and has no side effects
					pass = false;
				}
			}

			if (selectedSerializationModes.contains(SerializationMode.SERIALIZE_DESERIALIZE)) {
				clearModuleAndCollectorsAndChannels();
				serializationMode = SerializationMode.SERIALIZE_DESERIALIZE;
				if (!runTestCase()) {       // Test that serialization + deserialization works
					pass = false;
				}
			}

			return pass;
		} catch (TestHelperException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new TestHelperException(ex, this);
		}
	}

	/**
	 * Runs a test.
	 */
	public boolean runTestCase() throws IOException, ClassNotFoundException {
		beforeEachTestCase.call(module);
		int outputIndex = 0;
		for (int i = 0; i < inputValueCount + extraIterationsAfterInput; ++i) {

			// Time is set at start of event
			if (isTimedMode() && ticks.containsKey(i)) {
				((ITimeListener)module).setTime(ticks.get(i));
				module.getGlobals().time = ticks.get(i);
			}

			// Set input values
			if (i < inputValueCount) {
				serializeAndDeserializeModule();
				feedInputs(i);
			}

			// Activate module
			serializeAndDeserializeModule();
			activateModule(i);

			// Test outputs
			serializeAndDeserializeModule();
			if (shouldValidateOutput(i, skip)) {
				validateOutput(outputIndex, i);
				++outputIndex;
			}

			// Further global time
			furtherTime(i);
		}

		// End of data feed, destroy module
		module.destroy();

		// Test ui channel messages
		if (isUiChannelMode()) {
			validateUiChannelMessages();
		}

		afterEachTestCase.call();
		return true;
	}

	public boolean isClearStateCalled() {
		return clearStateCalled;
	}

	public SerializationMode getSerializationMode() {
		return serializationMode;
	}

	private void feedInputs(int i) {
		for (Map.Entry<String, List<Object>> entry : inputValuesByName.entrySet()) {
			Input input = getInputByEffectiveName(entry.getKey());
			if (input == null) {
				throw new IllegalArgumentException("No input found with name " + entry.getKey());
			}
			Object val = entry.getValue().get(i);
			if (val != null) {
				input.getSource().send(val);
			} else if (feedNullInputs) {
				input.receive(null);
			}
		}
	}

	private void activateModule(int i) {
		module.trySendOutput();
	}

	private boolean shouldValidateOutput(int i, int skip) {
		return i >= skip && (!isTimedMode() || ticks.containsKey(i));
	}

	private void validateOutput(int outputIndex, int i) {
		for (Map.Entry<String, List<Object>> entry : outputValuesByName.entrySet()) {

			Object actual = getOutputByEffectiveName(entry.getKey()).getTargets()[0].getValue();
			Object expected = entry.getValue().get(outputIndex);

			if (expected instanceof Double) {
				expected = DU.clean((Double)expected);
			}

			// Possible failures:
			// - An output value exists when it should not
			// - No output value exists when it should
			// - Incorrect output value is produced
			if ((expected == null && actual != null) ||
					(expected != null && actual == null) ||
					expected != null && !expected.equals(actual)) {
				throwException(entry.getKey(), i, outputIndex, actual, expected);
			}
		}
	}

	private void throwException(String outputName, int i, int outputIndex, Object value, Object target)  {
		String msg = "%s: mismatch at %d (inputIndex %d), was '%s' expected '%s'";
		throw new TestHelperException(String.format(msg, outputName, outputIndex, i, value, target), this);
	}

	private void furtherTime(int i) {
		if (i >= timeSteps.size()) {
			i = timeSteps.size() - 1;
		}

		int timeStep = timeSteps.get(i);

		if (timeStep != 0) {
			module.getGlobals().time = new Date(module.getGlobals().time.getTime() + timeStep);
		}
	}

	private void validateUiChannelMessages() {
		for (Map.Entry<String, List<Map>> expectedEntry : expectedUiChannelMessages.entrySet()) {
			String channel = expectedEntry.getKey();
			List<Map> expectedMessages = expectedEntry.getValue();

			if (!sentUiChannelMessages.containsKey(channel)) {
				throw new TestHelperException(String.format("uiChannel: channel '%s' was never pushed to", channel),
						this);
			}

			List<Map> actualMessages = sentUiChannelMessages.get(channel);
			for (int i = 0; i < Math.max(expectedMessages.size(), actualMessages.size()); ++i) {

				if (actualMessages.size() <= i) {
					String msg = String.format("uiChannel: expected %d messages on channel '%s' but received only %d",
							expectedMessages.size(), channel, actualMessages.size());
					throw new TestHelperException(msg, this);
				}

				if (expectedMessages.size() <= i) {
					String msg = "uiChannel: expected %d messages on channel '%s' but received %d additional: %s";
					throw new TestHelperException(String.format(msg,
							expectedMessages.size(),
							channel,
							actualMessages.size() - expectedMessages.size(),
							Arrays.toString(actualMessages.subList(i, actualMessages.size()).toArray())
					), this);
				}

				Object actual = actualMessages.get(i);
				Object expected = expectedMessages.get(i);

				if (!actual.equals(expected)) {
					String msg = "uiChannel: mismatch at %d, was '%s' expected '%s'";
					throw new TestHelperException(String.format(msg, i, actual, expected), this);
				}
			}

			sentUiChannelMessages.clear();
		}
	}

	private boolean isUiChannelMode() {
		return expectedUiChannelMessages != null;
	}

	private boolean isTimedMode() {
		return ticks != null;
	}

	private void serializeAndDeserializeModule() throws IOException, ClassNotFoundException {
		if (serializationMode != SerializationMode.NONE) {
			// Globals is transient, we need to restore it after deserialization
			Globals globalsTempHolder = module.getGlobals();

			module.beforeSerialization();
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			serializer.serialize(module, out);
			module.afterSerialization();

			if (serializationMode == SerializationMode.SERIALIZE_DESERIALIZE) {
				ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
				module = (AbstractSignalPathModule) serializer.deserialize(in);
				module.setGlobals(globalsTempHolder);
				if (module.getParentSignalPath() != null) {
					module.getParentSignalPath().setGlobals(globalsTempHolder);
				}
				module.afterDeserialization(dummySerializationService);
				moduleInstanceChanged.call(module);
			}
		}
	}

	private void clearModuleAndCollectorsAndChannels() {
		module.getGlobals().time = null;
		module.clear();
		setUpGlobals(module);

		for (Output<Object> output : module.getOutputs()) {
			for (Input<Object> target : output.getTargets()) {
				target.getOwner().setGlobals(new Globals());
				target.getOwner().clear();
				target.getOwner().setGlobals(null);
			}
		}
		module.connectionsReady();
	}

	private void turnOnUiChannelMode(Map<String, List<Map>> expectedMessages, Map<String, List<Map>> sentUiChannelMessages) {
		if (module instanceof ModuleWithUI) {
			this.expectedUiChannelMessages = new HashMap<>(expectedMessages);
			// Save reference, as the map doesn't hold anything yet
			this.sentUiChannelMessages = sentUiChannelMessages;
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
		outputValueCount = isTimedMode() ? ticks.size() : inputValueCount + extraIterationsAfterInput - skip;
		validateThatListSizesMatch();
		falsifyNoRepeats(module);
		initAndAttachOutputsToModuleInputs();
		connectCollectorsToModule(module);
		setUpGlobals(module);
		module.connectionsReady();
	}

	private void validateThatListSizesMatch() {
		if (skip > 0 && skip >= inputValueCount && extraIterationsAfterInput == 0) {
			throw new IllegalArgumentException("All values would be skipped and not a single output tested.");
		}
		for (List<Object> inputValues : inputValuesByName.values()) {
			if (inputValues.size() != inputValueCount) {
				String msg = String.format("Input value lists are not the same size (%d).", inputValueCount);
				throw new IllegalArgumentException(msg);
			}
		}
		for (Map.Entry<String, List<Object>> entry : outputValuesByName.entrySet()) {
			int outputListSize = entry.getValue().size();
			if (outputListSize != outputValueCount) {
				String msg = String.format("List for output '%s' not of expected size (%d != %d).",
					entry.getKey(),
					outputListSize,
					outputValueCount);
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

	private void setUpGlobals(AbstractSignalPathModule module) {
		if (module.getGlobals() == null) {
			module.setGlobals(new Globals());
		}
		module.getGlobals().time = new Date(0);
		module.setGlobals(overrideGlobalsClosure.call(module.getGlobals()));
	}

	/** create dummy outputs for each tested input */
	private void initAndAttachOutputsToModuleInputs() {
		for (String inputName : inputValuesByName.keySet()) {
			Input input = getInputByEffectiveName(inputName);
			if (input == null) {
				throw new IllegalArgumentException("No input found with name " + inputName);
			}
			Output output = new Output(null, "outputFor" + inputName, input.getTypeName());
			output.setDisplayName("outputFor" + inputName);
			output.connect(input);
		}
	}

	private Input getInputByEffectiveName(String name) {
		for (Input input : module.getInputs()) {
			if (name.equals(input.getEffectiveName())) {
				return input;
			}
		}
		return null;
	}

	private Output getOutputByEffectiveName(String name) {
		for (Output output : module.getOutputs()) {
			if (name.equals(output.getEffectiveName())) {
				return output;
			}
		}
		return null;
	}

	/** create (one-input dummy) Collector modules for each tested output */
	private void connectCollectorsToModule(AbstractSignalPathModule module) {
		for (String outputName : outputValuesByName.keySet()) {
			Output output = getOutputByEffectiveName(outputName);
			if (output == null) {
				throw new IllegalArgumentException("No output found with name " + outputName);
			}
			Collector collector = new Collector();
			collector.init();
			collector.attachToOutput(output);
		}
	}
	public static class Collector extends AbstractSignalPathModule {
		Input<Object> input = new Input<>(this, "input", "Object");

		@Override
		public void init() {
			addInput(input);
		}

		public void attachToOutput(Output<Object> externalOutput) {
			externalOutput.connect(input);
		}

		@Override
		public void sendOutput() { }

		@Override
		public void clearState() { }
	}
}