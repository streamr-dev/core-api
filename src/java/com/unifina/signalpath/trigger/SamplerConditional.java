package com.unifina.signalpath.trigger;

import com.unifina.signalpath.AbstractSignalPathModule;
import com.unifina.signalpath.TimeSeriesInput;
import com.unifina.signalpath.TimeSeriesOutput;

public class SamplerConditional extends AbstractSignalPathModule {

	TimeSeriesInput trigger = new TimeSeriesInput(this,"triggerIf");
	TimeSeriesInput value = new TimeSeriesInput(this,"value");
	
	TimeSeriesOutput out = new TimeSeriesOutput(this,"value");
	
	@Override
	public void init() {
		addInput(trigger);
		trigger.setDrivingInput(true);
		trigger.canToggleDrivingInput = false;
		trigger.canBeFeedback = false;
		trigger.canHaveInitialValue = false;
		
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
