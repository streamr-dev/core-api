package com.unifina.signalpath.utils;

import com.unifina.signalpath.TimeSeriesInput;

@Deprecated
public class Filter extends PassThrough {

	TimeSeriesInput pass = new TimeSeriesInput(this,"pass");
	
	@Override
	public void init() {
		addInput(pass);
		pass.setDrivingInput(false);
		pass.canToggleDrivingInput = false;
		addInput(input);
		input.setDrivingInput(true);
		pass.canToggleDrivingInput = true;
		addOutput(output);
	}
	
	@Override
	public void sendOutput() {
		if (pass.getValue().equals(1D))
			output.send(input.value);
	}

	@Override
	public void clearState() {

	}

}
