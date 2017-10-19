package com.unifina.signalpath.utils;

import com.unifina.signalpath.AbstractSignalPathModule;
import com.unifina.signalpath.Input;
import com.unifina.signalpath.Output;

import static com.unifina.signalpath.EndpointTypeCompatibilityChecker.acceptsAll;
import static com.unifina.signalpath.EndpointTypeCompatibilityChecker.areCompatible;

public class Merge extends AbstractSignalPathModule {

	Input<Object> inA = new Input<>(this,"A","Object");
	Input<Object> inB = new Input<>(this,"B","Object");
	Output<Object> out = new Output<>(this,"out","Object");
	
	@Override
	public void init() {
		addInput(inA);
		inA.setDrivingInput(true);
		inA.setCanToggleDrivingInput(false);
		inA.setReadyHack();
		addInput(inB);
		inB.setDrivingInput(true);
		inB.setCanToggleDrivingInput(false);
		inB.setReadyHack();
		addOutput(out);
	}

	@Override
	public void initialize() {
		// inA and inB must be of same type
		if (allEndpointsAreConnected()) {

			Output<Object> outA = inA.getSource();
			Output<Object> outB = inB.getSource();

			// Verify that the outputs connected to inputs are compatible
			if (!areCompatible(outA, outB)) {
				throw new RuntimeException("Merge: input types are not the same! A: " + outA.getTypeName()  + ", B: "
					+ outB.getTypeName());
			}
			
			// Verify that the inputs connected to this module's output are compatible with both inputs above
			for (Input i : out.getTargets()) {
				if (!areCompatible(i, outA) || !areCompatible(i, outB)) // Relation is not transitive, so 2 checks
					throw new RuntimeException("Merge: inputs are connected to type " + outA.getTypeName() +
						", connection to "+ (i.getDisplayName()!=null ? i.getDisplayName() : i.getName()) +
						" is of wrong type " + i.getTypeName()+"!");
			}
		}
	}

	private boolean allEndpointsAreConnected() {
		return inA.isConnected() && inB.isConnected() && out.getTargets().size() > 0;
	}

	@Override
	public void sendOutput() {
		if (getDrivingInputs().contains(inB))
			out.send(inB.value);
		if (getDrivingInputs().contains(inA))
			out.send(inA.value);
	}

	@Override
	public void clearState() {}

}
