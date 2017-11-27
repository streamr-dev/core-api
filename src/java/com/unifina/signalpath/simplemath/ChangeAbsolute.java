package com.unifina.signalpath.simplemath;

import com.unifina.signalpath.AbstractSignalPathModule;
import com.unifina.signalpath.TimeSeriesInput;
import com.unifina.signalpath.TimeSeriesOutput;

public class ChangeAbsolute extends AbstractSignalPathModule {

	class CustomTimeSeriesInput extends TimeSeriesInput {

		public CustomTimeSeriesInput(AbstractSignalPathModule owner, String name) {
			super(owner, name);
		}

		@Override
		public void setInitialValue(Double d) {
			super.setInitialValue(d);
			prev = d;
		}
	}

	TimeSeriesInput input = new CustomTimeSeriesInput(this,"in");

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
		if (prev==null && input.getInitialValue() != null)
			prev = input.getInitialValue();
		
		if (prev != null)
			out.send(input.value-prev); 
		
		prev = input.value;
	}
	
}
