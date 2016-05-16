package com.unifina.signalpath.map;

import com.unifina.signalpath.TimeSeriesInput;

public class SumByKey extends AggregateByKey {

	TimeSeriesInput value = new TimeSeriesInput(this, "value");

	@Override
	protected Double getNewWindowValue() {
		return value.getValue();
	}

}
