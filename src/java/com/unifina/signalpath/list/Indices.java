package com.unifina.signalpath.list;

import com.unifina.signalpath.AbstractSignalPathModule;
import com.unifina.signalpath.ListInput;
import com.unifina.signalpath.ListOutput;

import java.util.ArrayList;
import java.util.List;

public class Indices extends AbstractSignalPathModule {

	private final ListInput listIn = new ListInput(this, "list");
	private final ListOutput listOut = new ListOutput(this, "list");
	private final ListOutput indices = new ListOutput(this, "indices");

	@Override
	public void sendOutput() {
		List<Integer> indexList = new ArrayList<>();
		for (int i=0; i < listIn.getValue().size(); ++i) {
			indexList.add(i);
		}
		listOut.send(listIn.getValue());
		indices.send(indexList);
	}

	@Override
	public void clearState() {}
}
