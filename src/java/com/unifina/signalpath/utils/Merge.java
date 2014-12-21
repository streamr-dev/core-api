package com.unifina.signalpath.utils;

import com.unifina.signalpath.AbstractSignalPathModule;
import com.unifina.signalpath.Input;
import com.unifina.signalpath.Output;

public class Merge extends AbstractSignalPathModule {

	Input<Object> inA = new Input<>(this,"A","Object");
	Input<Object> inB = new Input<>(this,"B","Object");
	Output<Object> out = new Output<>(this,"out","Object");
	
	@Override
	public void init() {
		addInput(inA);
		inA.setDrivingInput(true);
		inA.canToggleDrivingInput = false;
		addInput(inB);
		inB.setDrivingInput(true);
		inB.canToggleDrivingInput = false;
		addOutput(out);
	}

	@Override
	public void initialize() {
		// inA and inB must be of same type
		if (inA.isConnected() && inB.isConnected() && out.getTargets().length>0) {
			String typeA = inA.getSource().getTypeName();
			String typeB = inB.getSource().getTypeName();
			
			// Only check input types if neither one of them is Object
			if (!typeA.equals("Object") && !typeB.equals("Object") && !typeA.equals(typeB))
				throw new RuntimeException("Merge: input types are not the same! A: "+typeA+", B: "+typeB);
			
			// output targest must be of same type as inA and inB, unless one of them is Object
			if (!typeA.equals("Object") && !typeB.equals("Object")) {
			for (Input i : out.getTargets())
				if (!typeA.equals(i.getTypeName()) && !i.getTypeName().equals("Object"))
					throw new RuntimeException("Merge: inputs are connected to type "+typeA+", connection to "+(i.getDisplayName()!=null ? i.getDisplayName() : i.getName())+" is of wrong type "+i.getTypeName()+"!");
			}
		}
	}
	
	@Override
	public void sendOutput() {
		if (drivingInputs.contains(inB))
			out.send(inB.value);
		if (drivingInputs.contains(inA))
			out.send(inA.value);
	}

	@Override
	public void clearState() {}

}
