package com.unifina.math;


public class MovingAverage extends Sum {

	public MovingAverage(int maLength) {
		super(maLength);
	}
	
	@Override
	public double getValue() {
		if (values.size()>0)
			return sum / values.size();
		else return 0;
	}
	
}
