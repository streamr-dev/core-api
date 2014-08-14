package com.unifina.signalpath.trigger;

import com.unifina.signalpath.AbstractSignalPathModule;
import com.unifina.signalpath.Input;
import com.unifina.signalpath.Output;
import com.unifina.signalpath.TimeSeriesInput;

public class SamplerConditional extends AbstractSignalPathModule {

	TimeSeriesInput trigger = new TimeSeriesInput(this,"triggerIf");
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
		if (drivingInputs.contains(trigger) && trigger.getValue().equals(1D)) {
			out.send(value.value);
		}
	}

	@Override
	public void clearState() {

	}

}
