package com.unifina.signalpath;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.unifina.signalpath.charts.TimeSeriesChart;

@SuppressWarnings("rawtypes")
public class Propagator {
	Set<Input> reachable;
	
	Set<AbstractSignalPathModule> originSet = new HashSet<>();
	ArrayList<AbstractSignalPathModule> origins = new ArrayList<>();
	ArrayList<Output> connectedOutputs = new ArrayList<>();
	
	AbstractSignalPathModule[] modArr;
	private boolean modArrDirty = true;
	
	public boolean sendPending = false;
	private boolean alwaysPropagate = false;
	
	private static final Logger log = Logger.getLogger(Propagator.class);
	
	public Propagator() {
		
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
			ArrayList<Output> outputs = new ArrayList<>();
			for (AbstractSignalPathModule m : origins)
				for (Output o : m.getOutputs())
					outputs.add(o);

			connectOutputs(outputs);
			reachable = makeReachableSet(outputs);
			calculateActivationOrder();
		}
	}
	
	public Propagator(Input[] inputs, AbstractSignalPathModule origin) {
		alwaysPropagate = true;
		reachable = makeReachableSet(inputs);
		calculateActivationOrder();
	}
	
//	@Deprecated
//	public Propagator(Output[] outputs) {
//		if (outputs.length>0) {
//			connectOutputs(Arrays.asList(outputs));
//			reachable = makeReachableSet(Arrays.asList(outputs));
//			calculateActivationOrder();
//		}
//	}
//	
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
	 * @param list
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
			// We can also skip modules in the originSet as they don't participate in dependency resolution
			if (i.isFeedbackConnection() || originSet.contains(module))
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
	 * Performs module dependency resolution and produces a sequence in which
	 * modules can be activated without violating the dependencies.
	 */
	private void calculateActivationOrder() {
		
		List<AbstractSignalPathModule> modules = new ArrayList<>();
		
		// Group the found inputs by module
		HashMap<AbstractSignalPathModule,Set<Input>> inputsByModule = new HashMap<>();
		for (Input i : reachable) {
			Set<Input> inputs = inputsByModule.get(i.getOwner());
			
			if (inputs==null) {
				inputs = new HashSet<Input>();
				inputsByModule.put(i.getOwner(), inputs);
			}
			inputs.add(i);
		}
		
		// Get first-order module dependencies via reachable inputs
		HashMap<AbstractSignalPathModule,Set<AbstractSignalPathModule>> forwardDependencies = new HashMap<>();
		HashMap<AbstractSignalPathModule,Set<AbstractSignalPathModule>> backwardDependencies = new HashMap<>();
		for (AbstractSignalPathModule module : inputsByModule.keySet()) {

			Set<AbstractSignalPathModule> bwdDepSet = backwardDependencies.get(module);
			if (bwdDepSet==null) {
				bwdDepSet = new HashSet<>();
				backwardDependencies.put(module, bwdDepSet);
			}
			
			for (Input i : module.getInputs()) {
				if (i.isConnected() && reachable.contains(i)) {
					// Modules in the originSet must not be added to dependency resolution, as
					// they have (by definition) already sent output prior to propagation.
					if (!originSet.contains(i.getSource().getOwner())) {
						bwdDepSet.add(i.getSource().getOwner());
					}
				}
			}
			
			Set<AbstractSignalPathModule> fwdDepSet = forwardDependencies.get(module);
			if (fwdDepSet==null) {
				fwdDepSet = new HashSet<>();
				forwardDependencies.put(module, fwdDepSet);
			}
			

			for (Output o : module.getOutputs()) {
				for (Input i : o.getTargets()) {
					// Modules in the originSet must not be added to dependency resolution, as
					// they have (by definition) already sent output prior to propagation.
					// Also we can't traverse inputs that are not in the reachable set (the input might have been excluded in the DFS step).
					if (reachable.contains(i) && !originSet.contains(i.getSource().getOwner())) {
						fwdDepSet.add(i.getOwner());
					}
				}
			}
		}
		
		while (!backwardDependencies.isEmpty()) {
			// Find a module that has no dependencies
			AbstractSignalPathModule noDep = null;
			for (AbstractSignalPathModule m : backwardDependencies.keySet()) {
				if (backwardDependencies.get(m).size()==0) {
					noDep = m;
					break;
				}
			}
			if (noDep==null)
				throw new RuntimeException("Deadlock!");
			
			modules.add(noDep);
			backwardDependencies.remove(noDep);
			
			if (forwardDependencies.get(noDep)!=null) {
				
				// Module noDep was activated and has sent output, update backwardDependencies to reflect this
				for (AbstractSignalPathModule m : forwardDependencies.get(noDep)) {
					backwardDependencies.get(m).remove(noDep);
				}
				
				forwardDependencies.remove(noDep);
			}
		}
		
		// This must hold or something's wrong
		if (modules.size()!=inputsByModule.keySet().size())
			throw new IllegalStateException("Not all modules were activated!");
		
		// Convert to array for speed
		modArr = modules.toArray(new AbstractSignalPathModule[modules.size()]);
		modArrDirty = false;
//		log.info("Propagator created for "+origin+": "+modules);
	}
	
	public void propagate() {
		// Lazy initialize if not explicitly called yet
		if (modArrDirty)
			initialize();
		
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

//	public void merge(Propagator propagator) {
//		reachable.addAll(propagator.reachable);
//		for (AbstractSignalPathModule m : propagator.origins)
//			if (!originSet.contains(m))
//				origins.add(m);
//		originSet.addAll(propagator.originSet);
//		
//		
//		
//		/**
//		 * Join the propagation arrays so that the arrays are concatenated and
//		 * in case of multiple instances of the same module the latter one is
//		 * preserved.
//		 */
//		HashSet<AbstractSignalPathModule> defer = new HashSet<>();
//		defer.addAll(propagator.getModules());
//		
//		ArrayList<AbstractSignalPathModule> newModArr = new ArrayList<>();
//		for (AbstractSignalPathModule m : modArr) {
//			if (defer.contains(m))
//				defer.remove(m);
//			else newModArr.add(m);
//		}
//		
//		newModArr.addAll(propagator.getModules());
//		modArr = newModArr.toArray(new AbstractSignalPathModule[newModArr.size()]);
//	}
	
}
