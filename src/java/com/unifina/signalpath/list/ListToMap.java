package com.unifina.signalpath.list;

import com.unifina.signalpath.AbstractSignalPathModule;
import com.unifina.signalpath.ListInput;
import com.unifina.signalpath.MapOutput;

import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;

public class ListToMap extends AbstractSignalPathModule {
	private final ListInput listIn = new ListInput(this, "list");
	private final MapOutput mapOut = new MapOutput(this, "map");

	@Override
	public void sendOutput() {
		List lst = listIn.getValue();
		Map<Double, Object> map = new LinkedHashMap<>();

		for (int i=0; i < lst.size(); ++i) {
			map.put((double) i, lst.get(i));
		}

		mapOut.send(map);
	}

	@Override
	public void clearState() {}
}
