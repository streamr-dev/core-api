package com.unifina.signalpath;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.log4j.Logger;

@SuppressWarnings("rawtypes")
public class Propagator {
	Set<Input> reachable;
	AbstractSignalPathModule origin;
	AbstractSignalPathModule[] modArr;
	
	public boolean sendPending = false;
	private boolean alwaysPropagate = false;
	
	private static final Logger log = Logger.getLogger(Propagator.class);
	
	public Propagator(Output[] outputs) {
		if (outputs.length>0) {
			origin = outputs[0].getOwner();
			connectOutputs(outputs);
			reachable = makeReachableSet(outputs);
			init();
		}
	}
	
	public Propagator(Input[] inputs, AbstractSignalPathModule origin) {
		this.origin = origin;
		alwaysPropagate = true;
		reachable = makeReachableSet(inputs);
		init();
	}
	
	private void connectOutputs(Output[] outputs) {
		// Let the Output know that it belongs to this Propagator
		for (Output o : outputs) {
			o.addPropagator(this);
		
			// Check that all outputs belong to the same module
			if (o.getOwner()!=origin)
				throw new IllegalArgumentException("All outputs must belong to the same module!");
		}
	}
	
	private Set<Input> makeReachableSet(Output[] outputs) {
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
	
	private void init() {
		
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
					if (i.getSource().getOwner()!=origin)
						bwdDepSet.add(i.getSource().getOwner());
				}
			}
			
			Set<AbstractSignalPathModule> fwdDepSet = forwardDependencies.get(module);
			if (fwdDepSet==null) {
				fwdDepSet = new HashSet<>();
				forwardDependencies.put(module, fwdDepSet);
			}
			
			for (Output o : module.getOutputs()) {
				for (Input i : o.getTargets()) {
					// This check is necessary (the input might be excluded in the DFS step)
					if (reachable.contains(i))
						fwdDepSet.add(i.getOwner());
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
		
//		log.info("Propagator created for "+origin+": "+modules);
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
	
	private void recurseInputs(Input[] inputs, Set<Input> visited) {
		for (Input i : inputs) {
			
			// Skip feedback connections, we don't want infinite loops!
			if (i.isFeedbackConnection())
				continue;
			
			if (!visited.contains(i)) {
				visited.add(i);
			}
			
			// Recurse to the owner if it's not an originatingModule
			AbstractSignalPathModule module = i.getOwner();
			if (!module.originatingModule) {
				for (Output o : module.getOutputs())
					recurseOutputs(o,visited);
			}
		}
	}
	
	public void propagate() {
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
