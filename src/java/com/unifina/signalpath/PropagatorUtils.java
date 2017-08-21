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
			boolean found = false;

			while (!found && !queue.isEmpty()) {
				AbstractSignalPathModule source = queue.remove();
				for (Input i : source.getInputs()) {
					if (i.isConnected()) {
						AbstractSignalPathModule outputOwner = i.getSource().getOwner();
						if (sources.contains(outputOwner)) {
							found = true;
							break;
						} else {
							queue.add(outputOwner);
						}
					}
				}
			}

			if (found) {
				result.add(targetModule);
			}
		}

		return result;
	}
}
