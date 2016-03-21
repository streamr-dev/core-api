package com.unifina.signalpath.random;

import com.unifina.signalpath.*;
import org.apache.commons.lang3.RandomStringUtils;

import java.util.Map;
import java.util.Random;

public class RandomString extends ModuleWithRandomness {
	private final IntegerParameter length = new IntegerParameter(this, "length", 10);
	private final Input<Object> trigger = new Input<>(this, "trigger", "Object");
	private final StringOutput out = new StringOutput(this, "out");

	private char[] symbols;

	public RandomString() {
		StringBuilder tmp = new StringBuilder();

		for (char ch = '0'; ch <= '9'; ++ch) {
			tmp.append(ch);
		}
		for (char ch = 'a'; ch <= 'z'; ++ch) {
			tmp.append(ch);
		}

		symbols = tmp.toString().toCharArray();
	}

	@Override
	public void init() {
		addInput(length);
		addInput(trigger);
		addOutput(out);
	}

	@Override
	public void sendOutput() {
		out.send(sampleString(symbols, length.getValue(), getRandom()));
	}

	public static String sampleString(char[] symbols, int length, Random random) {
		return RandomStringUtils.random(length, 0, symbols.length, false, false, symbols, random);
	}

	@Override
	public Map<String, Object> getConfiguration() {
		Map<String, Object> config = super.getConfiguration();

		ModuleOptions options = ModuleOptions.get(config);
		options.addIfMissing(new ModuleOption("symbols", new String(symbols), "string"));

		return config;
	}

	@Override
	protected void onConfiguration(Map<String, Object> config) {
		super.onConfiguration(config);

		ModuleOptions options = ModuleOptions.get(config);
		ModuleOption symbolsOption = options.getOption("symbols");
		if (symbolsOption != null) {
			symbols = symbolsOption.getString().toCharArray();
		}
	}
}
