package com.unifina.signalpath.list;

import com.unifina.signalpath.AbstractSignalPathModule;
import com.unifina.signalpath.Input;
import com.unifina.signalpath.ListInput;
import com.unifina.signalpath.TimeSeriesOutput;

public class IndexOfItem extends AbstractSignalPathModule {
	private final ListInput list = new ListInput(this, "list");
	private final Input<Object> item = new Input<>(this, "item", "Object");
	private final TimeSeriesOutput index = new TimeSeriesOutput(this, "index");

	@Override
	public void sendOutput() {
		int i = list.getValue().indexOf(item.getValue());
		if (i != -1) {
			index.send((double)i);
		}
	}

	@Override
	public void clearState() {}
}
