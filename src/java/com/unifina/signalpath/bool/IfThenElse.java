package com.unifina.signalpath.bool;

import com.unifina.signalpath.GroovySignalPathModule;
import com.unifina.signalpath.TimeSeriesInput;
import com.unifina.signalpath.TimeSeriesOutput;

public class IfThenElse extends GroovySignalPathModule {

	TimeSeriesInput condition = new TimeSeriesInput(this,"if");
	TimeSeriesInput th = new TimeSeriesInput(this,"then");
	TimeSeriesInput el = new TimeSeriesInput(this,"else");
	
	TimeSeriesOutput out = new TimeSeriesOutput(this,"out");
	
	@Override
	public void init() {
		addInput(condition);
		addInput(th);
		addInput(el);
		addOutput(out);
	}
	
	@Override
	public void sendOutput() {
		if (condition.value==1)
			out.send(th.value);
		else
			out.send(el.value);
	}

	@Override
	public void clearState() {

	}

}
