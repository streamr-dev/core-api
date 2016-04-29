package com.unifina.signalpath.variadic;

import com.unifina.signalpath.*;
import com.unifina.utils.IdGenerator;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Abstract base class providing utilities for managing the lifecycle of a variable number of endpoints of
 * pre-determined type.
 *
 * @param <E> endpoint type
 * @param <T> data type
 */
abstract class VariadicEndpoint<E extends Endpoint<T>, T> implements Serializable {
	private final AbstractSignalPathModule module;
	private final String configOption;
	private final List<E> endpoints = new ArrayList<>();
	private int numOfEndpoints;

	public void getConfiguration(Map<String,Object> config) {
		ModuleOptions options = ModuleOptions.get(config);
		options.addIfMissing(ModuleOption.createInt(configOption, numOfEndpoints));
	}

	public void onConfiguration(Map<String,Object> config) {
		ModuleOptions options = ModuleOptions.get(config);

		ModuleOption option = options.getOption(configOption);
		if (option != null) {
			numOfEndpoints = option.getInt();
		}

		for (int i = 0; i < numOfEndpoints; ++i) {
			addEndpoint(i);
		}
	}

	public List<E> getEndpoints() {
		return Collections.unmodifiableList(endpoints);
	}

	VariadicEndpoint(AbstractSignalPathModule module, String configOption, int defaultCount) {
		this.module = module;
		this.configOption = configOption;
		this.numOfEndpoints = defaultCount;
	}

	abstract E makeAndAttachNewEndpoint(AbstractSignalPathModule owner);

	abstract String getDisplayName();

	private void addEndpoint(int index) {
		E endpoint = makeAndAttachNewEndpoint(module);
		endpoint.setName("endpoint-" + IdGenerator.get());
		endpoint.setDisplayName(numOfEndpoints == 1 ? getDisplayName() : getDisplayName() + (index + 1));
		endpoints.add(endpoint);
	}
}
