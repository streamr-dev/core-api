package com.unifina.signalpath.utils;

import com.unifina.signalpath.AbstractSignalPathModule;
import com.unifina.signalpath.Input;
import com.unifina.signalpath.Output;

@Deprecated
public class PassThrough extends AbstractSignalPathModule {

	Input<Object> input = new Input<>(this,"in","Object");
	Output<Object> output = new Output<>(this,"out","Object");
	
	@Override
	public void init() {
		addInput(input);
		addOutput(output);
	}
	
	@Override
	public void initialize() {
		// Input source and Output targets must be of same type
		if (input.isConnected() && output.getTargets().size()>0) {
			String type = input.getSource().getTypeName();
			
			// TODO: For now, skip the complex situation in which two Object 
			// endpoints are connected, for example two PassThroughs chained
			if (!type.equals("Object")) {
				for (Input i : output.getTargets())
					if (!i.getTypeName().equals("Object") && !type.equals(i.getTypeName()))
						throw new RuntimeException("PassThrough: input is connected to type "+type+", connection to "+(i.getDisplayName()!=null ? i.getDisplayName() : i.getName())+" is of wrong type ("+i.getTypeName()+")!");
			}
		}
	}
	
	@Override
	public void sendOutput() {
		output.send(input.value);
	}

	@Override
	public void clearState() {

	}

}
