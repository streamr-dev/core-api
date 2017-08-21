package com.unifina.signalpath;

import org.apache.log4j.Logger;

import java.io.Serializable;
import java.util.*;

import static com.unifina.signalpath.PropagatorUtils.findDependentModules;
import static com.unifina.signalpath.PropagatorUtils.findReachableSetFromInputs;
import static com.unifina.signalpath.PropagatorUtils.findReachableSetFromOutputs;

public class Propagator implements Serializable {
	private final Set<AbstractSignalPathModule> originSet = new LinkedHashSet<>();
	private Set<AbstractSignalPathModule> reachable;
	private AbstractSignalPathModule[] activationOrder;
	private boolean dirty = true;

	// Outputs associated with this Propagator set sendPending to true when sending output
	public boolean sendPending = false;
	private boolean alwaysPropagate = false;
	
	private static final Logger log = Logger.getLogger(Propagator.class);
	
	public Propagator() {}

	public Propagator(AbstractSignalPathModule origin) {
		addModule(origin);
	}

	public Propagator(List<Input> inputs) {
		alwaysPropagate = true;
		reachable = findReachableSetFromInputs(inputs);
		calculateActivationOrder();
	}

	public void addModule(AbstractSignalPathModule module) {
		boolean newModule = originSet.add(module);
		if (newModule) {
			dirty = true;
		}
	}
	
	public void initialize() {
		if (dirty) {
			sendPending = true;

			List<Output> outputs = new ArrayList<>();
			for (AbstractSignalPathModule m : originSet) {
				Collections.addAll(outputs, m.getOutputs());
			}

			registerPropagator(outputs);
			reachable = findReachableSetFromOutputs(outputs);
			calculateActivationOrder();
		}
	}
	
	private void registerPropagator(List<Output> outputs) {
		for (Output o : outputs) {
			o.addPropagator(this);
		}
	}

	/**
	 * Performs module dependency resolution and produces a sequence in which
	 * modules can be activated without violating the dependencies.
	 */
	private void calculateActivationOrder() {
		// Find and remove inter-dependencies in originSet
		Set<AbstractSignalPathModule> independentOriginSet = new HashSet<>(originSet);
		independentOriginSet.removeAll(findDependentModules(originSet, originSet));

		// Build up dependency graph
		DependencyGraph<AbstractSignalPathModule> dependencyGraph = new DependencyGraph<>();
		for (AbstractSignalPathModule module : reachable) {

			// Resolve 1st order backward dependencies by reachable inputs
			Set<AbstractSignalPathModule> bwdDepSet = new HashSet<>();
			for (Input i : module.getInputs()) {
				if (i.isConnected() && reachable.contains(i.getSource().getOwner())) {
					// Modules in the independentOriginSet must not be added to dependency resolution.
					// It would cause a deadlock or a module never activating, depending on traversal order.
					if (!independentOriginSet.contains(i.getSource().getOwner())) {
						bwdDepSet.add(i.getSource().getOwner());
					}
				}
			}

			// Resolve 1st order forward dependencies by reachable outputs
			Set<AbstractSignalPathModule> fwdDepSet = new HashSet<>();
			for (Output o : module.getOutputs()) {
				for (Input i : (List<Input>) o.getTargets()) {
					// Also we can't traverse inputs that are not in the reachable set (the input might have been excluded in the DFS step).
					// Note that module targets can't be in the independentOriginSet by definition, so that doesn't need to be checked
					if (reachable.contains(i.getOwner())) {
						fwdDepSet.add(i.getOwner());
					}
				}
			}

			dependencyGraph.put(module, bwdDepSet, fwdDepSet);
		}
		
		// Retrieve module activation order by topologically sorting dependency graph.
		List<AbstractSignalPathModule> activationOrder = dependencyGraph.topologicalSort();
		
		// Invariant: all reachable modules must be present in activation sequence
		if (activationOrder.size() != reachable.size()) {
			throw new IllegalStateException("Not all modules were activated!");
		}
		
		// Convert to array for speed
		this.activationOrder = activationOrder.toArray(new AbstractSignalPathModule[activationOrder.size()]);
		dirty = false;
	}
	
	public void propagate() {
		// Lazy initialize if not explicitly called yet
		if (dirty) {
			long startTime = System.currentTimeMillis();
			initialize();
			long diffInSecs = System.currentTimeMillis() - startTime;
			log.info("Initialized Propagator with " + activationOrder.length + " modules in " + diffInSecs + " ms.");
		}
		
		if (sendPending || alwaysPropagate) {
			sendPending = false;
			for (AbstractSignalPathModule module : activationOrder) {
				module.trySendOutput();
			}
		}
	}
	
	public void clear() {
		for (AbstractSignalPathModule module : activationOrder) {
			module.clear();
		}
	}
}
