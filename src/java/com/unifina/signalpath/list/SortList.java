package com.unifina.signalpath.list;

import com.unifina.signalpath.AbstractSignalPathModule;
import com.unifina.signalpath.ListInput;
import com.unifina.signalpath.ListOutput;
import com.unifina.signalpath.map.SortMap;

import java.util.Collections;
import java.util.List;

public class SortList extends AbstractSignalPathModule {
	private final SortMap.OrderParameter order =
		new SortMap.OrderParameter(this, "order", SortMap.OrderParameter.ASCENDING);
	private final ListInput in = new ListInput(this, "in");
	private final ListOutput out = new ListOutput(this, "out");

	@Override
	public void sendOutput() {
		List list = in.getModifiableValue();
		if (order.isDescending()) {
			Collections.sort(list, Collections.reverseOrder());
		} else {
			Collections.sort(list);
		}
		out.send(list);
	}

	@Override
	public void clearState() {}
}
