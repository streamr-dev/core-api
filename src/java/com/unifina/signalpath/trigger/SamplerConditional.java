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
		trigger.canToggleDrivingInput = false;
		trigger.canBeFeedback = false;
		
		addInput(value);
		value.canToggleDrivingInput = false;
		value.setDrivingInput(false);
		
		addOutput(out);
	}
	
	@Override
	public void sendOutput() {
		if (drivingInputs.contains(trigger) && trigger.getValue()) {
			out.send(value.value);
		}
	}

	@Override
	public void clearState() {

	}

}
