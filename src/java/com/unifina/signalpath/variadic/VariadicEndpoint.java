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
	private final EndpointInstantiator<E> endpointInstantiator;
	private final String countConfig;
	private final String namesConfig;
	private final List<E> endpoints = new ArrayList<>();
	private int numOfEndpoints;

	public void getConfiguration(Map<String,Object> config) {
		ModuleOptions options = ModuleOptions.get(config);
		options.addIfMissing(ModuleOption.createInt(countConfig, numOfEndpoints));
		config.put(namesConfig, collectEndpointNames());
	}

	public void onConfiguration(Map<String,Object> config) {
		ModuleOptions options = ModuleOptions.get(config);

		ModuleOption count = options.getOption(countConfig);
		if (count != null) {
			numOfEndpoints = count.getInt();
		}

		List<String> existingEndpointNames = (List<String>) config.get(namesConfig);
		for (int i = 0; i < numOfEndpoints; ++i) {
			String name;
			if (existingEndpointNames != null && i < existingEndpointNames.size()) {
				name = existingEndpointNames.get(i);
			} else {
				name = "endpoint-" + IdGenerator.get();
			}
			addEndpoint(i, name);
		}
	}

	public List<E> getEndpoints() {
		return Collections.unmodifiableList(endpoints);
	}

	VariadicEndpoint(AbstractSignalPathModule module,
					 EndpointInstantiator<E> endpointInstantiator,
					 String countConfig,
					 String namesConfig,
					 int defaultCount) {
		this.module = module;
		this.endpointInstantiator = endpointInstantiator;
		this.countConfig = countConfig;
		this.namesConfig = namesConfig;
		this.numOfEndpoints = defaultCount;
	}

	abstract void attachToModule(AbstractSignalPathModule owner, E endpoint);

	abstract String getDisplayName();

	private void addEndpoint(int index, String name) {
		E endpoint = endpointInstantiator.instantiate(module, name);
		endpoint.setDisplayName(numOfEndpoints == 1 ? getDisplayName() : getDisplayName() + (index + 1));
		endpoints.add(endpoint);
		attachToModule(module, endpoint);
	}

	private List<String> collectEndpointNames() {
		List<String> names = new ArrayList<>();
		for (E e : endpoints) {
			names.add(e.getName());
		}
		return names;
	}
}
