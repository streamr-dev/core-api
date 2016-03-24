package com.unifina.signalpath.map;

import com.unifina.signalpath.*;

import java.util.LinkedHashMap;
import java.util.Map;

public class ConstantMap extends AbstractSignalPathModule implements Pullable<Map<String, Object>> {

	MapParameter map = new MapParameter(this, "map", new LinkedHashMap<String, Object>());
	MapOutput out = new MapOutput(this,"out");

	public ConstantMap() {
		super();
		initPriority = 40;
	}
	
	@Override
	public void init() {
		addInput(map);
		addOutput(out);
	}
	
	@Override
	public void initialize() {
		for (Input i : out.getTargets())
			i.receive(map.getValue());
	}
	
	@Override
	public void sendOutput() {
		out.send(map.getValue());
	}

	@Override
	public void clearState() {

	}

	@Override
	public Map<String, Object> pullValue(Output output) {
		return map.getValue();
	}
	
}
