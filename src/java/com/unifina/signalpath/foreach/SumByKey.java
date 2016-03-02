package com.unifina.signalpath.foreach;

import com.unifina.math.Sum;
import com.unifina.signalpath.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class SumByKey extends AggregateByKey {

	IntegerParameter windowLength = new IntegerParameter(this, "windowLength", 0);

	TimeSeriesInput value = new TimeSeriesInput(this, "value");

	private Map<String, Sum> sumByKey = new HashMap<>();

	@Override
	protected double onSendOutput(String key, Double currentValue) {
		Sum sum = sumByKey.get(key);
		if (sum == null) {
			sum = new Sum(windowLength.getValue());
			sumByKey.put(key, sum);
		}
		return sum.add(value.getValue());
	}

	@Override
	protected void onKeyDropped(String key, Double lastValue) {
		sumByKey.remove(key);
	}

	@Override
	public void clearState() {
		super.clearState();
		sumByKey = new HashMap<>();
	}
}
