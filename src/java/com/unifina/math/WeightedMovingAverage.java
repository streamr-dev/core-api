package com.unifina.math;

import java.util.List;

public class WeightedMovingAverage extends MovingAverage {

	List<Double> weights;
	Double cachedValue = null;

	public WeightedMovingAverage(List<Double> weights) {
		super(weights.size());
		this.weights = weights;
	}

	@Override
	public double getValue() {
		if (cachedValue==null) {
			double result = 0D;
			double weightSum = 0D;
			int offset = weights.size() - values.size();
			for (int i=0;i<values.size();i++) {
				result += weights.get(i+offset)*values.get(i);
				weightSum += weights.get(i+offset);
			}
			cachedValue = result / weightSum;
		}
		
		return cachedValue;
	}

	public List<Double> getWeights() {
		return weights;
	}

	@Override
	public double add(double p) {
		cachedValue = null;
		return super.add(p);
	}
	
	public void setWeights(List<Double> weights) {
		this.weights = weights;
		super.setLength(weights.size());
		cachedValue = null;
	}
	
	@Override
	public void setLength(int length) {
		throw new RuntimeException("Set length by calling setWeights(List<Double>)!");
	}
	
}
