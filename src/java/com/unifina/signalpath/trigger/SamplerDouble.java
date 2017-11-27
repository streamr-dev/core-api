package com.unifina.signalpath.trigger;

import com.unifina.signalpath.AbstractSignalPathModule;
import com.unifina.signalpath.TimeSeriesInput;
import com.unifina.signalpath.TimeSeriesOutput;

// No use anymore, can be replaced with two Samplers and Merge
@Deprecated
public class SamplerDouble extends AbstractSignalPathModule {
	
	TimeSeriesInput triggerA = new TimeSeriesInput(this,"trigA");
	TimeSeriesInput valueA = new TimeSeriesInput(this,"valA");

	TimeSeriesInput triggerB = new TimeSeriesInput(this,"trigB");
	TimeSeriesInput valueB = new TimeSeriesInput(this,"valB");
	
		
	TimeSeriesOutput out = new TimeSeriesOutput(this,"value");
	
	@Override
	public void init() {
		addInput(triggerA);
		addInput(valueA);
		addInput(triggerB);
		addInput(valueB);
		addOutput(out);
	}
	
	@Override
	public void sendOutput() {
		if (getDrivingInputs().contains(triggerA)) {
			out.send(valueA.value);
		}
		if (getDrivingInputs().contains(triggerB)) {
			out.send(valueB.value);
		}
	}

	@Override
	public void clearState() {

	}

}
