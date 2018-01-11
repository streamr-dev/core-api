package com.unifina.signalpath.trigger;

import com.unifina.signalpath.AbstractSignalPathModule;
import com.unifina.signalpath.Input;
import com.unifina.signalpath.Output;

public class Sampler extends AbstractSignalPathModule {

	Input<Object> trigger = new Input<>(this,"trigger","Object");
	Input<Object> value = new Input<>(this,"value","Object");
	
	Output<Object> out = new Output<Object>(this,"value","Object");
	
	@Override
	public void init() {
		addInput(trigger);
		trigger.setDrivingInput(true);
		trigger.setCanToggleDrivingInput(false);
		
		addInput(value);
		value.setDrivingInput(false);
		value.setCanToggleDrivingInput(false);
		
		addOutput(out);
	}
	
	@Override
	public void sendOutput() {
		if (getDrivingInputs().contains(trigger)) {
			out.send(value.value);
		}
	}

	@Override
	public void clearState() {

	}

}
