package com.unifina.math;

import com.unifina.utils.SlidingDoubleArray;

import java.io.Serializable;

public class StandardDeviation implements IWindowedOperation, Serializable {

	private int length;

	private MovingAverage mean;
	private SlidingDoubleArray values;
	
	transient org.apache.commons.math3.stat.descriptive.moment.StandardDeviation sd = new org.apache.commons.math3.stat.descriptive.moment.StandardDeviation();
	
	public StandardDeviation(int length) {
		this.length = length;
		mean = new MovingAverage(length);
		values = new SlidingDoubleArray(length);
	}
	
	@Override
	public int getLength() {
		return length;
	}

	@Override
	public int size() {
		return mean.size();
	}

	@Override
	public void setLength(int length) {
		this.length = length;
		mean.setLength(length);
		values.changeSize(length);
	}

	@Override
	public double add(double p) {
		mean.add(p);
		values.add(p);
		return getValue();
	}

	@Override
	public double getValue() {
		if (sd == null) {
			sd = new org.apache.commons.math3.stat.descriptive.moment.StandardDeviation();
		}
		return sd.evaluate(values.getValues(), mean.getValue());
	}

	@Override
	public void clear() {
		mean.clear();
		values.clear();
		sd.clear();
	}

}
