package com.unifina.signalpath.variadic;

import com.unifina.signalpath.*;

import java.io.Serializable;
import java.util.*;

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
	private final List<E> endpoints = new ArrayList<>();
	private int offsetIndex;

	public E addEndpoint(String name) {
		E endpoint = initializeEndpoint(name);
		endpoints.add(endpoint);
		attachToModule(module, endpoint);
		updateEndpointConfigurations();
		return endpoint;
	}

	public List<E> getEndpoints() {
		if (endpoints.isEmpty()) {
			return new ArrayList<>();
		}
		return endpoints.subList(0, endpoints.size() - 1);
	}


	public void onConfiguration(Map<String, Object> config) {
		if (endpoints.isEmpty()) {
			addPlaceholder();
		} else {
			E placeholder = endpoints.get(endpoints.size() - 1);
			furtherConfigurePlaceholder(placeholder);
			attachToModule(module, placeholder);
		}
	}

	List<E> getEndpointsIncludingPlaceholder() {
		return endpoints;
	}

	AbstractSignalPathModule getModule() {
		return module;
	}

	VariadicEndpoint(AbstractSignalPathModule module,
					 EndpointInstantiator<E> endpointInstantiator) {
		this(module, endpointInstantiator, 1);
	}

	VariadicEndpoint(AbstractSignalPathModule module,
					 EndpointInstantiator<E> endpointInstantiator,
					 int startIndex) {
		this.module = module;
		this.endpointInstantiator = endpointInstantiator;
		this.offsetIndex = startIndex;
	}

	/**
	 * Sub class is responsible for attaching <code>endpoint</code> to <code>owner</code> in a correct way.
	 */
	abstract void attachToModule(AbstractSignalPathModule owner, E endpoint);

	/**
	 * Sub class can further configure placeholder if deemed necessary.
	 */
	abstract void furtherConfigurePlaceholder(E placeholder);

	/**
	 * Sub class defines (prefix) display name of endpoints
	 */
	abstract String getDisplayName();

	/**
	 * Sub class defines js class that will be used in front-end.
	 */
	abstract String getJsClass();

	private E addPlaceholder() {
		E endpoint = initializeEndpoint("endpoint-" + System.currentTimeMillis());
		furtherConfigurePlaceholder(endpoint);
		endpoints.add(endpoint);
		attachToModule(module, endpoint);
		updateEndpointConfigurations();
		return endpoint;
	}

	private E initializeEndpoint(String name) {
		E endpoint = endpointInstantiator.instantiate(module, name);
		endpoint.setDisplayName(getDisplayName() + (offsetIndex + endpoints.size()));
		endpoint.setJsClass(getJsClass());
		return endpoint;
	}

	private void updateEndpointConfigurations() {
		for (int i=0; i < endpoints.size(); ++i) {
			E e = endpoints.get(i);
			Map config = e.getConfiguration();

			boolean isLast = i == endpoints.size() - 1;

			Map<String, Object> variadicConfig = new HashMap<>();
			variadicConfig.put("index", offsetIndex + i);
			variadicConfig.put("isLast", isLast);

			config.put("variadic", variadicConfig);
			e.setConfiguration(config);
		}
	}
}
