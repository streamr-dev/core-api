package com.unifina.signalpath.list;

import com.unifina.signalpath.AbstractSignalPathModule;
import com.unifina.signalpath.BooleanParameter;
import com.unifina.signalpath.ListInput;
import com.unifina.signalpath.ListOutput;

import java.util.ArrayList;
import java.util.List;

public class FlattenList extends AbstractSignalPathModule {
	private final BooleanParameter deep = new BooleanParameter(this, "deep", false);
	private final ListInput in = new ListInput(this, "in");
	private final ListOutput out = new ListOutput(this, "out");

	@Override
	public void sendOutput() {
		out.send(flattenList(in.getValue(), deep.getValue()));
	}

	@Override
	public void clearState() {}

	private static List flattenList(List list, boolean deep) {
		List newList = new ArrayList();
		for (Object o : list) {
			if (o instanceof List) {
				if (deep) {
					o = flattenList((List) o, true);
				}
				for (Object o2 : (List) o) {
					newList.add(o2);
				}
			} else {
				newList.add(o);
			}
		}
		return newList;
	}
}
