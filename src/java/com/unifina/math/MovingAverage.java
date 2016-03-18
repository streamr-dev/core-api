package com.unifina.math;


public class MovingAverage extends Sum {

	private long count = 0;

	public MovingAverage(int maLength) {
		super(maLength);
	}

	@Override
	public double add(double p) {
		count++;
		return super.add(p);
	}

	@Override
	public double getValue() {
		if (count>0)
			return sum / count;
		else return 0;
	}

	@Override
	public void clear() {
		super.clear();
		count = 0;
	}

	@Override
	protected int purgeExtraValues() {
		int purged = super.purgeExtraValues();
		count -= purged;
		return purged;
	}
}
