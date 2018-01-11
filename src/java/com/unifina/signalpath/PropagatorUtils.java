package com.unifina.signalpath;

import java.util.*;

public class PropagatorUtils {

	public static Set<AbstractSignalPathModule> findReachableSetFromOutputs(List<Output> outputs) {
		List<Input> inputs = new ArrayList<>();
		for (Output o : outputs) {
			inputs.addAll(o.getTargets());
		}
		return findReachableSetFromInputs(inputs);
	}

	public static Set<AbstractSignalPathModule> findReachableSetFromInputs(List<Input> inputs) {
		Set<AbstractSignalPathModule> reachableSet = new HashSet<>();
		Stack<Input> stack = new Stack<>();
		stack.addAll(inputs);

		// DFS walk through module graph
		while (!stack.isEmpty()) {
			Input input = stack.pop();
			AbstractSignalPathModule module = input.getOwner();

			if (!reachableSet.contains(module)) {
				reachableSet.add(module);

				// Recurse to the owner if it's not an originatingModule
				if (!module.isPropagationSink()) {
					for (Output o : module.getOutputs()) {
						stack.addAll(o.getTargets());
					}
				}
			}
		}

		return reachableSet;
	}

	/**
	 * Filters <code>targets</code>, returning those modules that depend on any module in <code>sources</code>.
	 * Dependencies are found by traversing connections backwards starting from each module in <code>targets</code>.
	 */
	public static Set<AbstractSignalPathModule> findDependentModules(Set<AbstractSignalPathModule> targets,
																	 Set<AbstractSignalPathModule> sources) {
		Set<AbstractSignalPathModule> result = new HashSet<>();

		for (AbstractSignalPathModule targetModule : targets) {
			Queue<AbstractSignalPathModule> queue = new ArrayDeque<>();
			queue.add(targetModule);

			// Keep track of visited modules to improve efficiency and break infinite loops
			Set<AbstractSignalPathModule> visited = new HashSet<>();

			// Use a named loop to be able to break out of the nested loop
			backwardsSourceSearch:
			while (!queue.isEmpty()) {
				AbstractSignalPathModule currentModule = queue.remove();
				visited.add(currentModule);

				for (Input i : currentModule.getInputs()) {
					if (i.isConnected()) {
						// Traverse "backwards" via the current input to the module that connects to it
						AbstractSignalPathModule outputOwner = i.getSource().getOwner();

						// Is that a source module?
						if (sources.contains(outputOwner)) {
							// The current target depends on one of the source modules!
							result.add(targetModule);
							break backwardsSourceSearch;
						} else if (!visited.contains(outputOwner)) {
							// No dependency found yet, queue
							queue.add(outputOwner);
						}
					}
				}
			}
		}

		return result;
	}

}
