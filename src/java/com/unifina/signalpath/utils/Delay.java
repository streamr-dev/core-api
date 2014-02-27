package com.unifina.signalpath.utils;

import java.util.ArrayDeque;

import com.unifina.signalpath.AbstractSignalPathModule;
import com.unifina.signalpath.Input;
import com.unifina.signalpath.IntegerParameter;
import com.unifina.signalpath.Output;

public class Delay extends AbstractSignalPathModule {

	IntegerParameter length = new IntegerParameter(this,"delayEvents",1);
	Input<Object> input = new Input<>(this,"in","Object");
	Output<Object> out = new Output<>(this,"out","Object");
	
	ArrayDeque<Object> window = new ArrayDeque<>();
	
	@Override
	public void init() {
		addInput(length);
		addInput(input);
		addOutput(out);
	}
	
	@Override
	public void initialize() {
		// Input source and Output targets must be of same type
		if (input.isConnected() && out.getTargets().length>0) {
			String type = input.getSource().getTypeName();
			
			for (Input i : out.getTargets())
				if (!type.equals(i.getTypeName()))
					throw new RuntimeException("Delay: input is connected to type "+type+", connection to "+(i.getDisplayName()!=null ? i.getDisplayName() : i.getName())+" is of wrong type ("+i.getTypeName()+")!");
		}
	}
	
	@Override
	public void sendOutput() {
		while (window.size()>length.getValue())
			window.removeFirst();
		
		if (window.size()==length.getValue()) {
			out.send(window.removeFirst());
		}
		
		window.add(input.value);
	}
	
	@Override
	public void clearState() {
		window.clear();
	}

}
