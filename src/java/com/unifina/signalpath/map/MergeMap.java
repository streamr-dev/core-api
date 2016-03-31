package com.unifina.signalpath.map;

import com.unifina.signalpath.AbstractSignalPathModule;
import com.unifina.signalpath.MapInput;
import com.unifina.signalpath.MapOutput;

import java.util.Map;

public class MergeMap extends AbstractSignalPathModule {

	private MapInput leftMap = new MapInput(this, "leftMap");
	private MapInput rightMap = new MapInput(this, "rightMap");
	private MapOutput out = new MapOutput(this, "out");

	@Override
	public void sendOutput() {
		Map map = leftMap.getModifiableValue();
		map.putAll(rightMap.getValue());
		out.send(map);
	}

	@Override
	public void clearState() {

	}
}
