package com.unifina.signalpath.map;

import com.unifina.signalpath.AbstractSignalPathModule;
import com.unifina.signalpath.IntegerParameter;
import com.unifina.signalpath.MapInput;
import com.unifina.signalpath.MapOutput;

import java.util.Map;

abstract class LimitMap extends AbstractSignalPathModule {
	private IntegerParameter limit = new IntegerParameter(this, "limit", 50);
	private MapInput in = new MapInput(this, "in");
	private MapOutput out = new MapOutput(this, "out");

	@Override
	public void init() {
		super.init();
		addInput(limit);
		addInput(in);
		addOutput(out);
	}

	protected abstract Map makeLimitedCopyOfMap(Map source, Integer limit);

	@Override
	public void sendOutput() {
		out.send(makeLimitedCopyOfMap(in.getValue(), limit.getValue()));
	}


	@Override
	public void clearState() {}
}
