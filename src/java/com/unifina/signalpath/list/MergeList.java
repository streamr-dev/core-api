package com.unifina.signalpath.list;

import com.unifina.signalpath.AbstractSignalPathModule;
import com.unifina.signalpath.ListInput;
import com.unifina.signalpath.ListOutput;

import java.util.List;

public class MergeList extends AbstractSignalPathModule {
	private final ListInput head = new ListInput(this, "head");
	private final ListInput tail = new ListInput(this, "tail");
	private final ListOutput out = new ListOutput(this, "out");

	@Override
	public void sendOutput() {
		List newList = head.getModifiableValue();
		newList.addAll(tail.getValue());
		out.send(newList);
	}

	@Override
	public void clearState() {}
}
