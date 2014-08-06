package com.unifina.signalpath.simplemath;

import com.unifina.signalpath.AbstractSignalPathModule;
import com.unifina.signalpath.TimeSeriesInput;
import com.unifina.signalpath.TimeSeriesOutput;

public class ChangeAbsolute extends AbstractSignalPathModule {

	TimeSeriesInput input = new TimeSeriesInput(this,"in") {
		@Override
		public void setInitialValue(Double d) {
			super.setInitialValue(d);
			prev = d;
		}
	};
	
	TimeSeriesOutput out = new TimeSeriesOutput(this,"out");
	
	Double prev = null;
	
	@Override
	public void init() {
		addInput(input);
		addOutput(out);
	}
	
	public void clearState() {
		prev = null;
	}
	
	public void sendOutput() {
		if (prev==null && input.initialValue!=null)
			prev = input.initialValue;
		
		if (prev != null)
			out.send(input.value-prev); 
		
		prev = input.value;
	}
	
}
