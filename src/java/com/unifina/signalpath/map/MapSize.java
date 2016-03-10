package com.unifina.signalpath.map;

import com.unifina.signalpath.*;

public class MapSize extends AbstractSignalPathModule {

	private MapInput in = new MapInput(this, "in");
	private TimeSeriesOutput size = new TimeSeriesOutput(this, "size");

	@Override
	public void sendOutput() {
		size.send(in.getValue().size());
	}

	@Override
	public void clearState() {}
}
