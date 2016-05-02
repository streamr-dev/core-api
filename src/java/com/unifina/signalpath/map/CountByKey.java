package com.unifina.signalpath.map;

public class CountByKey extends AggregateByKey {

	@Override
	protected Double getNewWindowValue() {
		return 1D;
	}

}
