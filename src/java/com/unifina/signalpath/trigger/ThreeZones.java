package com.unifina.signalpath.trigger;

import com.unifina.signalpath.AbstractSignalPathModule;
import com.unifina.signalpath.DoubleParameter;
import com.unifina.signalpath.TimeSeriesInput;
import com.unifina.signalpath.TimeSeriesOutput;

public class ThreeZones extends AbstractSignalPathModule {

	DoubleParameter highZone = new DoubleParameter(this,"highZone",0.8D);
	DoubleParameter lowZone = new DoubleParameter(this,"lowZone",-0.8D);
	
	TimeSeriesInput input = new TimeSeriesInput(this,"in");
	TimeSeriesOutput out = new TimeSeriesOutput(this,"out");
	
	@Override
	public void init() {
		addInput(highZone);
		addInput(lowZone);
		addInput(input);
		addOutput(out);
	}
	
	@Override
	public void sendOutput() {
		
		double currentZone;
		if (input.value >= highZone.getValue())
			currentZone = 1D;
		else if (input.value <= lowZone.getValue())
			currentZone = -1D;
		else 
			currentZone = 0D;
		
		out.send(currentZone);
	}
	
	@Override
	public void clearState() {
		
	}
	
}
