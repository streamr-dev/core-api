package com.unifina.signalpath.simplemath;

import com.unifina.signalpath.AbstractSignalPathModule;
import com.unifina.signalpath.Input;
import com.unifina.signalpath.TimeSeriesOutput;

public class Count extends AbstractSignalPathModule {

	Input<Object> input = new Input<>(this,"in","Object");
	TimeSeriesOutput out = new TimeSeriesOutput(this, "count");
	
	double count = 0;
	
	@Override
	public void sendOutput() {
		count += 1;
		out.send(count);
	}

	@Override
	public void clearState() {
		count = 0;
	}

}
