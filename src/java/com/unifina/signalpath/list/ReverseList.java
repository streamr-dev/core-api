package com.unifina.signalpath.list;

import com.unifina.signalpath.AbstractSignalPathModule;
import com.unifina.signalpath.ListInput;
import com.unifina.signalpath.ListOutput;

import java.util.Collections;
import java.util.List;

public class ReverseList extends AbstractSignalPathModule {
	private final ListInput in = new ListInput(this, "in");
	private final ListOutput out = new ListOutput(this, "out");

	@Override
	public void sendOutput() {
		List list = in.getModifiableValue();
		Collections.reverse(list);
		out.send(list);
	}

	@Override
	public void clearState() {}
}
