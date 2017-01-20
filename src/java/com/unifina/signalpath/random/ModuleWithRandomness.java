package com.unifina.signalpath.random;

import com.unifina.signalpath.*;

import java.util.Map;
import java.util.Random;

/**
 * Handle configuring, clearing, and seeding of <code>Random</code>. Let derived class handle
 * actual sampling logic.
 */
abstract class ModuleWithRandomness extends AbstractSignalPathModule {
	private Random random;
	private int seed = new Random().nextInt();

	protected Random getRandom() {
		return random;
	}

	@Override
	public Map<String, Object> getConfiguration() {
		Map<String, Object> config = super.getConfiguration();
		ModuleOptions options = ModuleOptions.get(config);
		options.addIfMissing(new ModuleOption("seed", seed, "int"));
		return config;
	}

	@Override
	protected void onConfiguration(Map<String, Object> config) {
		super.onConfiguration(config);

		ModuleOptions options = ModuleOptions.get(config);
		ModuleOption seedOption = options.getOption("seed");
		if (seedOption != null) {
			seed = seedOption.getInt();
		}
		random = new Random(seed);
	}

	@Override
	public void clearState() {
		random = new Random(seed);
	}
}
