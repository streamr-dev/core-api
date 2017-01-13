package com.unifina.signalpath;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

@SuppressWarnings("rawtypes")
public class Propagator implements Serializable {
	private Set<Input> reachable;
	
	private Set<AbstractSignalPathModule> originSet = new HashSet<>();
	private ArrayList<AbstractSignalPathModule> origins = new ArrayList<>();
	
	private AbstractSignalPathModule[] modArr;
	private boolean modArrDirty = true;

	// Outputs associated with this Propagator set sendPending to true when sending output
	public boolean sendPending = false;
	private boolean alwaysPropagate = false;
	
	private static final Logger log = Logger.getLogger(Propagator.class);
	
	public Propagator() {}

	public Propagator(AbstractSignalPathModule origin) {
		addModule(origin);
	}

	public Propagator(Input[] inputs, AbstractSignalPathModule origin) {
		alwaysPropagate = true;
		reachable = makeReachableSet(inputs);
		calculateActivationOrder();
	}

	public Propagator(List<Input> inputs, AbstractSignalPathModule origin) {
		this(inputs.toArray(new Input[inputs.size()]), origin);
	}

	public void addModule(AbstractSignalPathModule module) {
		boolean newModule = originSet.add(module);
		if (newModule) { 
			origins.add(module);
			modArrDirty = true;
		}
	}
	
	public void initialize() {
		if (modArrDirty) {
			sendPending = true;

			ArrayList<Output> outputs = new ArrayList<>();
			for (AbstractSignalPathModule m : origins) {
				for (Output o : m.getOutputs()) {
					outputs.add(o);
				}
			}

			connectOutputs(outputs);
			reachable = makeReachableSet(outputs);
			calculateActivationOrder();
		}
	}
	
	private void connectOutputs(List<Output> outputs) {
		// Let the Output know that it belongs to this Propagator
		for (Output o : outputs) {
			o.addPropagator(this);
		}
	}
	
	private Set<Input> makeReachableSet(List<Output> outputs) {
		// Search all inputs that are reachable from the provided outputs
		Set<Input> set = new HashSet<Input>();
		for (Output o : outputs)
			recurseOutputs(o,set);
		
		return set;
	}
	
	private Set<Input> makeReachableSet(Input[] inputs) {
		// Search all inputs that are reachable from the input owners, including the starting inputs
		Set<Input> set = new HashSet<Input>();
		recurseInputs(inputs,set);
		return set;
	}
	

	/**
	 * DFS search of inputs reachable from specified output
	 * @param output
	 * @param visited
	 */
	private void recurseOutputs(Output output, Set<Input> visited) {
		Input[] inputs = output.getTargets();
		recurseInputs(inputs,visited);
	}
	
	/**
	 * DFS walk through the connections in this graph. The visited inputs are
	 * collected to the visited set, which becomes the "reachable" object.
	 * @param inputs
	 * @param visited
	 */
	private void recurseInputs(Input[] inputs, Set<Input> visited) {
		for (Input i : inputs) {
			AbstractSignalPathModule module = i.getOwner();
			
			// Skip feedback connections, we don't want infinite dependency loops!
			if (i.isFeedbackConnection())
				continue;
			
			if (!visited.contains(i)) {
				visited.add(i);
			}
			
			// Recurse to the owner if it's not an originatingModule
			if (!module.propagationSink) {
				for (Output o : module.getOutputs())
					recurseOutputs(o,visited);
			}
		}
	}

	/**
	 * Returns the items in the origins set that depend directly or indirectly on one of the modules in dependantsToFind set.
	 * @param origins
	 * @param dependantsToFind
	 * @return
     */
	private Set<AbstractSignalPathModule> findDependentModules(Set<AbstractSignalPathModule> origins, Set<AbstractSignalPathModule> dependantsToFind) {
		Set<AbstractSignalPathModule> result = new HashSet<>();

		for (AbstractSignalPathModule mod : origins) {
			Set<AbstractSignalPathModule> sources = new HashSet<>();

			for (Input i : mod.getInputs()) {
				if (i.isConnected() && dependantsToFind.contains(i.getSource().getOwner())) {
					result.add(i.getOwner());
				}
				else if (i.isConnected()) {
					sources.add(i.getSource().getOwner());
				}
			}

			if (!sources.isEmpty()) {
				// Do any of the sources depend on those asked? If yes, then this module is also dependant
				Set<AbstractSignalPathModule> dependantSources = findDependentModules(sources, dependantsToFind);
				if (!dependantSources.isEmpty()) {
					result.add(mod);
				}
			}
		}

		return result;
	}

	/**
	 * Performs module dependency resolution and produces a sequence in which
	 * modules can be activated without violating the dependencies.
	 */
	private void calculateActivationOrder() {
		// Find the modules that are reachable
		Set<AbstractSignalPathModule> reachableModules = new HashSet<>(reachable.size());
		for (Input i : reachable) {
			reachableModules.add(i.getOwner());
		}

		// Find and remove interdependencies in originSet, we need this soon
		Set<AbstractSignalPathModule> independentOriginSet = new HashSet<>(originSet);
		independentOriginSet.removeAll(findDependentModules(originSet, originSet));

		// Build up dependency graph
		DependencyGraph<AbstractSignalPathModule> dependencyGraph = new DependencyGraph<>();
		for (AbstractSignalPathModule module : reachableModules) {

			// Resolve 1st order backward dependencies by reachable inputs
			Set<AbstractSignalPathModule> bwdDepSet = new HashSet<>();
			for (Input i : module.getInputs()) {
				if (i.isConnected() && reachable.contains(i)) {
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
				for (Input i : o.getTargets()) {
					// Also we can't traverse inputs that are not in the reachable set (the input might have been excluded in the DFS step).
					// Note that module targets can't be in the independentOriginSet by definition, so that doesn't need to be checked
					if (reachable.contains(i)) {
						fwdDepSet.add(i.getOwner());
					}
				}
			}

			dependencyGraph.put(module, bwdDepSet, fwdDepSet);
		}
		
		// Retrieve module activation order by topologically sorting dependency graph.
		List<AbstractSignalPathModule> activationOrder = dependencyGraph.topologicalSort();
		
		// Invariant: all reachable modules must be present in activation sequence
		if (activationOrder.size() != reachableModules.size()) {
			throw new IllegalStateException("Not all modules were activated!");
		}
		
		// Convert to array for speed
		modArr = activationOrder.toArray(new AbstractSignalPathModule[activationOrder.size()]);
		modArrDirty = false;
	}
	
	public void propagate() {
		// Lazy initialize if not explicitly called yet
		if (modArrDirty) {
			long startTime = System.currentTimeMillis();
			log.info("Initializing Propagator");
			initialize();
			double diffInSecs = (System.currentTimeMillis() - startTime) / 1000.0;
			log.info("Initialization of Propagator finished. Handled " + modArr.length + " modules. Took " + diffInSecs + " secs.");
		}
		
		if (sendPending || alwaysPropagate) {
			sendPending = false;
			for (int i=0; i<modArr.length;i++) {
				modArr[i].trySendOutput();
			}
		}
	}
	
	public List<AbstractSignalPathModule> getModules() {
		return Arrays.asList(modArr);
	}
	
	public void clear() {
		for (int i=0; i<modArr.length;i++) {
			modArr[i].clear();
		}
	}
}
