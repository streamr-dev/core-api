package com.unifina.signalpath;

import java.util.*;

/**
 * Provides topological sorting (dependency resolution) of a DAG (directed acyclic graph) using Kahn's algorithm.
 */
class DependencyGraph<T> {
	private final Map<T, Set<T>> backwardDependencies = new HashMap<>();
	private final Map<T, Set<T>> forwardDependencies = new HashMap<>();
	private final List<T> independentNodes = new ArrayList<>();

	void put(T node, Set<T> backwardDeps, Set<T> forwardDeps) {
		if (forwardDependencies.containsKey(node)) {
			throw new IllegalArgumentException("Dependent " + node + " is already in the graph.");
		}

		if (backwardDeps.isEmpty()) {
			independentNodes.add(node);
		} else {
			backwardDependencies.put(node, backwardDeps);
		}

		forwardDependencies.put(node, forwardDeps);
	}

	List<T> topologicalSort() {
		List<T> sorted = new ArrayList<>(forwardDependencies.size());
		while (!isEmpty()) {
			sorted.add(pop());
		}
		if (forwardDependencies.isEmpty() && backwardDependencies.isEmpty()) {
			return sorted;
		} else {
			throw new RuntimeException("Deadlocked! Not all dependencies could be resolved.");
		}
	}

	private boolean isEmpty() {
		return independentNodes.isEmpty();
	}

	private T pop() {
		T independentNode = independentNodes.remove(independentNodes.size() - 1);
		Set<T> forwardDependentNodes = forwardDependencies.remove(independentNode);

		// Remove independent node from dependents' (backward) dependencies. Then check whether any became independent.
		for (T dependentNode : forwardDependentNodes) {
			Set<T> backwardDeps = backwardDependencies.get(dependentNode);
			backwardDeps.remove(independentNode);
			if (backwardDeps.isEmpty()) {
				backwardDependencies.remove(dependentNode);
				independentNodes.add(dependentNode);
			}
		}

		return independentNode;
	}
}
