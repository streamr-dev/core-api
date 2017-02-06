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
public abstract class VariadicEndpoint<E extends Endpoint<T>, T> implements Serializable {
	private final String baseName;
	private final AbstractSignalPathModule module;
	private final EndpointInstantiator<E> endpointInstantiator;
	private final List<E> endpoints = new ArrayList<>();
	private int startIndex;
	private int numOfEndpoints = 1;

	public String getBaseName() {
		return baseName;
	}

	public List<E> getEndpoints() {
		if (endpoints.isEmpty()) {
			return new ArrayList<>();
		}
		return endpoints.subList(0, endpoints.size() - 1);
	}


	public void setConfiguration(Map<String, Object> config) {
		if (config.containsKey("numOfEndpoints")) {
			numOfEndpoints = (int) config.get("numOfEndpoints");
		}
		for (int i=0; i < numOfEndpoints; ++i) {
			addEndpoint(i);
		}
		configurePlaceholder(endpoints.get(endpoints.size() - 1));
	}


	public Map<String, Object> getConfiguration() {
		Map<String, Object> config = new HashMap<>();
		config.put("baseName", baseName);
		config.put("numOfEndpoints", numOfEndpoints);
		config.put("startIndex", startIndex);
		return config;
	}

	List<E> getEndpointsIncludingPlaceholder() {
		return endpoints;
	}

	AbstractSignalPathModule getModule() {
		return module;
	}

	VariadicEndpoint(String baseName, AbstractSignalPathModule module, EndpointInstantiator<E> endpointInstantiator) {
		this(baseName, module, endpointInstantiator, 1);
	}

	VariadicEndpoint(String baseName,
					 AbstractSignalPathModule module,
					 EndpointInstantiator<E> endpointInstantiator,
					 int startIndex) {
		this.baseName = baseName;
		this.module = module;
		this.endpointInstantiator = endpointInstantiator;
		this.startIndex = startIndex;
	}

	/**
	 * Sub class is responsible for attaching <code>endpoint</code> to <code>owner</code> in a correct way.
	 */
	abstract void attachToModule(AbstractSignalPathModule owner, E endpoint);

	/**
	 * Sub class can further configure placeholder if necessary.
	 */
	abstract void configurePlaceholder(E placeholder);

	/**
	 * Sub class defines js class that will be used in front-end.
	 */
	abstract String getJsClass();

	private E addEndpoint(int idx) {
		E endpoint = initializeEndpoint(baseName + (startIndex + idx));
		endpoints.add(endpoint);
		attachToModule(module, endpoint);
		return endpoint;
	}

	private E initializeEndpoint(String name) {
		E endpoint = endpointInstantiator.instantiate(module, name);
		endpoint.setJsClass(getJsClass());
		return endpoint;
	}
}
