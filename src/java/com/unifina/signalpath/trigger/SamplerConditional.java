package com.unifina.signalpath.trigger;

import com.unifina.signalpath.*;

public class SamplerConditional extends AbstractSignalPathModule {

	BooleanInput trigger = new BooleanInput(this,"triggerIf");
	Input<Object> value = new Input<>(this,"value","Object");
	
	Output<Object> out = new Output<>(this,"value","Object");
	
	@Override
	public void init() {
		addInput(trigger);
		trigger.setDrivingInput(true);
		trigger.setCanToggleDrivingInput(false);
		
		addInput(value);
		value.setCanToggleDrivingInput(false);
		value.setDrivingInput(false);
		
		addOutput(out);
	}
	
	@Override
	public void sendOutput() {
		if (getDrivingInputs().contains(trigger) && trigger.getValue()) {
			out.send(value.value);
		}
	}

	@Override
	public void clearState() {

	}

}
