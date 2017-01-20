package com.unifina.signalpath.list;

import com.unifina.signalpath.AbstractSignalPathModule;
import com.unifina.signalpath.ListInput;
import com.unifina.signalpath.TimeSeriesOutput;

public class ListSize extends AbstractSignalPathModule {

	private final ListInput in = new ListInput(this, "in");
	private final TimeSeriesOutput size = new TimeSeriesOutput(this, "size");

	@Override
	public void sendOutput() {
		size.send(in.getValue().size());
	}

	@Override
	public void clearState() {}
}
