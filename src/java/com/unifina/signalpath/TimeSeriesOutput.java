package com.unifina.signalpath;

import com.unifina.utils.DU;

public class TimeSeriesOutput extends PrimitiveOutput<Double> {
	
	public TimeSeriesOutput(AbstractSignalPathModule owner, String name) {
		super(owner, name, "Double");
	}
	
	@Override
	public String getTypeName() {
		return "Double";
	}

	public void send(int value) {
		send(new Double(value));
	}

	public void send(long value) {
		send(new Double(value));
	}

	public void send(Double value) {
		super.send(DU.clean(value));
	}
	
}
