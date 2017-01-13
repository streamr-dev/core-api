package com.unifina.signalpath;

import java.util.*;

/**
 * Provides the means for building up a DAG (directed acyclic graph) and then performing a topological sort
 * (dependency resolution) on it.
 */
class DependencyGraph<T> {
	private final Map<T, Set<T>> allBackwardDeps = new HashMap<>();
	private final Map<T, Set<T>> allForwardDeps = new HashMap<>();
	private final Queue<T> keysByDependencyCount = new PriorityQueue<>(11,
		new Comparator<T>() {
			@Override
			public int compare(T o1, T o2) {
				return Integer.compare(allBackwardDeps.get(o1).size(), allBackwardDeps.get(o2).size());
			}
	});

	void put(T dependent, Set<T> backwardDependencies, Set<T> forwardDependencies) {
		if (allBackwardDeps.containsKey(dependent)) {
			throw new IllegalArgumentException("Dependent " + dependent + " has been already put.");
		}
		allBackwardDeps.put(dependent, backwardDependencies);
		allForwardDeps.put(dependent, forwardDependencies);
		keysByDependencyCount.add(dependent);
	}

	List<T> topologicalSort() {
		List<T> modules = new ArrayList<>(keysByDependencyCount.size());
		while (!isEmpty()) {
			modules.add(pop());
		}
		return modules;
	}

	private boolean isEmpty() {
		return keysByDependencyCount.isEmpty();
	}

	private T pop() {
		T nonDependentModule = keysByDependencyCount.remove();
		Set<T> backwardDependencies = allBackwardDeps.remove(nonDependentModule);
		Set<T> forwardDependencies = allForwardDeps.remove(nonDependentModule);

		if (!backwardDependencies.isEmpty()) {
			String msg = "Propagation deadlocked! Module " + nonDependentModule + " still has dependencies "
				+ backwardDependencies + " but should be empty.";
			throw new RuntimeException(msg);
		}

		for (T dependency : forwardDependencies) {
			allBackwardDeps.get(dependency).remove(nonDependentModule);

			// Backward dependency count has changed so update position in queue.
			keysByDependencyCount.remove(dependency);
			keysByDependencyCount.add(dependency);
		}

		return nonDependentModule;
	}
}
