package com.unifina.signalpath.list;

import com.unifina.signalpath.*;

import java.util.ArrayList;
import java.util.List;

public class SubList extends AbstractSignalPathModule {
	private final IntegerParameter from = new IntegerParameter(this, "from", 0);
	private final IntegerParameter to = new IntegerParameter(this, "to", 1);
	private final ListInput in = new ListInput(this, "in");
	private final ListOutput out = new ListOutput(this, "out");
	private final StringOutput error = new StringOutput(this, "error");

	@Override
	public void sendOutput() {
		List unserializableSubList = null;
		try {
			unserializableSubList = in.getValue().subList(from.getValue(), to.getValue());
		} catch (IndexOutOfBoundsException | IllegalArgumentException e) {
			error.send(e.getMessage());
		}

		if (unserializableSubList != null) {
			List subList = new ArrayList<>(unserializableSubList); // TODO: performance optimization
			out.send(subList);
		}
	}

	@Override
	public void clearState() {}
}
