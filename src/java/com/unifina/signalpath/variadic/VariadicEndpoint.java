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
	private int numOfModules;

	public void getConfiguration(Map<String,Object> config) {
		ModuleOptions options = ModuleOptions.get(config);
		options.addIfMissing(ModuleOption.createInt(configOption, numOfModules));
	}

	public void onConfiguration(Map<String,Object> config) {
		ModuleOptions options = ModuleOptions.get(config);

		ModuleOption option = options.getOption(configOption);
		if (option != null) {
			numOfModules = option.getInt();
		}

		for (int i=0; i < numOfModules; ++i) {
			addEndpoint(i);
		}
	}

	public List<E> getEndpoints() {
		return Collections.unmodifiableList(endpoints);
	}

	VariadicEndpoint(AbstractSignalPathModule module, String configOption, int defaultCount) {
		this.module = module;
		this.configOption = configOption;
		this.numOfModules = defaultCount;
	}

	abstract E makeAndAttachNewEndpoint(AbstractSignalPathModule owner, int index);

	private void addEndpoint(int index) {
		E endpoint = makeAndAttachNewEndpoint(module, index + 1);
		if (endpoint.getDisplayName() == null) {
			endpoint.setDisplayName(endpoint.getName());
		}
		endpoint.setName("endpoint-" + IdGenerator.get());
		endpoints.add(endpoint);
	}
}
