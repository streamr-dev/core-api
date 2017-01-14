package com.unifina.signalpath;

import java.util.*;

public class PropagatorUtils {

	public static Set<AbstractSignalPathModule> findReachableSetFromOutputs(List<Output> outputs) {
		List<Input> inputs = new ArrayList<>();
		for (Output o : outputs) {
			Collections.addAll(inputs, o.getTargets());
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

			// Skip feedback connections, we don't want infinite dependency loops!
			if (!input.isFeedbackConnection() && !reachableSet.contains(module)) {
				reachableSet.add(module);

				// Recurse to the owner if it's not an originatingModule
				if (!module.propagationSink) {
					for (Output o : module.getOutputs()) {
						Collections.addAll(stack, o.getTargets());
					}
				}
			}
		}

		return reachableSet;
	}

	/**
	 * Finds the items in the <code>origins</code> that are reachable from <code>dependantsToFind</code>.
	 */
	public static Set<AbstractSignalPathModule> findDependentModules(Set<AbstractSignalPathModule> origins,
																	 Set<AbstractSignalPathModule> dependentsToFind) {
		Set<AbstractSignalPathModule> result = new HashSet<>();

		for (AbstractSignalPathModule originModule : origins) {
			Queue<AbstractSignalPathModule> queue = new ArrayDeque<>();
			queue.add(originModule);
			boolean found = false;

			while (!found && !queue.isEmpty()) {
				AbstractSignalPathModule source = queue.remove();
				for (Input i : source.getInputs()) {
					if (i.isConnected()) {
						AbstractSignalPathModule outputOwner = i.getSource().getOwner();
						if (dependentsToFind.contains(outputOwner)) {
							found = true;
							break;
						} else {
							queue.add(outputOwner);
						}
					}
				}
			}

			if (found) {
				result.add(originModule);
			}
		}

		return result;
	}
}
