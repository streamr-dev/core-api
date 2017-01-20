package com.unifina.signalpath.list;

import com.unifina.signalpath.AbstractSignalPathModule;
import com.unifina.signalpath.IntegerParameter;
import com.unifina.signalpath.ListInput;
import com.unifina.signalpath.ListOutput;

import java.util.List;

public class RemoveFromList extends AbstractSignalPathModule {
	private final IntegerParameter index = new IntegerParameter(this, "index", 0);
	private final ListInput in = new ListInput(this, "in");
	private final ListOutput out = new ListOutput(this, "out");

	@Override
	public void sendOutput() {
		int i = index.getValue();
		if (i < 0) {
			i = in.getValue().size() + i;
		}

		if (i >= 0 && i < in.getValue().size()) {
			List list = in.getModifiableValue();
			list.remove(i);
			out.send(list);
		} else {
			out.send(in.getValue());
		}
	}

	@Override
	public void clearState() {}
}
