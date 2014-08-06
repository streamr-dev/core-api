package com.unifina.signalpath.trigger;

import com.unifina.signalpath.AbstractSignalPathModule;
import com.unifina.signalpath.TimeSeriesInput;
import com.unifina.signalpath.TimeSeriesOutput;

public class Sampler extends AbstractSignalPathModule {

	TimeSeriesInput trigger = new TimeSeriesInput(this,"trigger");
	TimeSeriesInput value = new TimeSeriesInput(this,"value");
	
	TimeSeriesOutput out = new TimeSeriesOutput(this,"value");
	
	@Override
	public void init() {
		addInput(trigger);
		trigger.setDrivingInput(true);
		trigger.canToggleDrivingInput = false;
		trigger.canHaveInitialValue = false;
		trigger.canBeFeedback = false;
		
		addInput(value);
		value.setDrivingInput(false);
		value.canToggleDrivingInput = false;
		
		addOutput(out);
	}
	
	@Override
	public void sendOutput() {
		if (drivingInputs.contains(trigger)) {
			out.send(value.value);
		}
	}

	@Override
	public void clearState() {

	}

}
