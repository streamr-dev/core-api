package com.unifina.signalpath.trigger;

import com.unifina.signalpath.AbstractSignalPathModule;
import com.unifina.signalpath.BooleanParameter;
import com.unifina.signalpath.DoubleParameter;
import com.unifina.signalpath.TimeSeriesInput;
import com.unifina.signalpath.TimeSeriesOutput;

public class ZeroCross extends AbstractSignalPathModule {

	DoubleParameter threshold = new DoubleParameter(this,"threshold",0D);
	BooleanParameter strictMode = new BooleanParameter(this,"strictMode",true);
	
	TimeSeriesInput input = new TimeSeriesInput(this,"in");
	TimeSeriesOutput out = new TimeSeriesOutput(this,"out");
	
	Double prevIndValue = null;
	
	@Override
	public void init() {
		addInput(strictMode);
		addInput(threshold);
		addInput(input);
		addOutput(out);
	}
	
	@Override
	public void sendOutput() {
		if (prevIndValue==null)
			prevIndValue = (strictMode.getValue() ? input.value : 0);
		
		double th = threshold.getValue();
		
		if (prevIndValue < th && input.value > th) {
			out.send(1D);
		}
		// Groovy seems to give 0 > -0 == true if bound to variables, watch out!
		else if (prevIndValue > (th==0 ? 0 : -th) && input.value < (th==0 ? 0 : -th)) {
			out.send(-1D);
		}
		// Else just repeat the previous value
		else if (out.getValue() != null)
			out.send(out.getValue());
			
		prevIndValue = input.value;
		
		// Always produce a value
		if (out.getValue() != null)
			out.send(out.getValue());
	}
	
	public void clearState() {
		prevIndValue = null;
	}

}
