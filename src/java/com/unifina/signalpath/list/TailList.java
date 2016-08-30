package com.unifina.signalpath.list;

import com.unifina.signalpath.AbstractSignalPathModule;
import com.unifina.signalpath.IntegerParameter;
import com.unifina.signalpath.ListInput;
import com.unifina.signalpath.ListOutput;

import java.util.ArrayList;

public class TailList extends AbstractSignalPathModule {
	private final IntegerParameter limit = new IntegerParameter(this, "limit", 50);
	private final ListInput in = new ListInput(this, "in");
	private final ListOutput out = new ListOutput(this, "out");

	@Override
	public void sendOutput() {
		int lowerBound = Math.max(0, in.getValue().size() - limit.getValue());
		int upperBound = in.getValue().size();
		out.send(new ArrayList(in.getValue().subList(lowerBound, upperBound))); // TODO: cannot serialize sublist
	}

	@Override
	public void clearState() {}
}
