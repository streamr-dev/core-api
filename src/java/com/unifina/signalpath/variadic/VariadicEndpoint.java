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
	private final String namesConfig;
	private final List<E> endpoints = new ArrayList<>();
	private E placeholderEndpoint;
	private int currentIndex = 1;

	public void getConfiguration(Map<String,Object> config) {
		config.put(namesConfig, collectEndpointNames());
	}

	public void onConfiguration(Map<String,Object> config) {
		List<String> existingEndpointNames = (List<String>) config.get(namesConfig);
		if (existingEndpointNames != null) {
			for (String endpointName : existingEndpointNames) {
				addEndpoint(endpointName);
			}
		}
		addPlaceholderEndpoint();
	}

	public List<E> getEndpoints() {
		return Collections.unmodifiableList(endpoints);
	}

	public E getPlaceholder() {
		return placeholderEndpoint;
	}

	VariadicEndpoint(AbstractSignalPathModule module,
					 EndpointInstantiator<E> endpointInstantiator,
					 String namesConfig) {
		this.module = module;
		this.endpointInstantiator = endpointInstantiator;
		this.namesConfig = namesConfig;
	}

	VariadicEndpoint(AbstractSignalPathModule module,
					 EndpointInstantiator<E> endpointInstantiator,
					 String namesConfig,
					 int startIndex) {
		this(module, endpointInstantiator, namesConfig);
		this.currentIndex = startIndex;
	}

	abstract void attachToModule(AbstractSignalPathModule owner, E endpoint);

	abstract void handlePlaceholder(E placeholderEndoint);

	abstract String getDisplayName();

	private void addEndpoint(String name) {
		E endpoint = initializeEndpoint(name);
		endpoints.add(endpoint);
		attachToModule(module, endpoint);
	}

	private void addPlaceholderEndpoint() {
		placeholderEndpoint = initializeEndpoint("endpoint-" + IdGenerator.get());
		handlePlaceholder(placeholderEndpoint);
		attachToModule(module, placeholderEndpoint);
	}

	private E initializeEndpoint(String name) {
		E endpoint = endpointInstantiator.instantiate(module, name);
		endpoint.setDisplayName(getDisplayName() + (currentIndex++));
		return endpoint;
	}

	private List<String> collectEndpointNames() {
		List<String> names = new ArrayList<>();
		for (E e : endpoints) {
			names.add(e.getName());
		}
		return names;
	}
}
