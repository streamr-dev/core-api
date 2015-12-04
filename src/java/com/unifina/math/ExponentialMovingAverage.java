package com.unifina.math;

import java.io.Serializable;

public class ExponentialMovingAverage implements IWindowedOperation, Serializable {
	MovingAverage sma;
	Double ema = null;
	int length;
	double alpha;
	boolean smaInit;
	
	public ExponentialMovingAverage(int length, boolean smaInit) {
		this.length = length;
		sma = new MovingAverage(length);
		alpha = calculateAlpha(length);
		this.smaInit = smaInit;
	}
	
	public ExponentialMovingAverage(int length) {
		this(length, true);
	}
	
	public double calculateAlpha(int length) {
		return 2.0 / (length + 1.0);
	}
	
	public int getLength() {
		return length;
	}
	
	public void setLength(int length) {
		this.length = length;
		
		if (ema==null)
			sma.setLength(length);

		alpha = calculateAlpha(length);
	}
	
	public double add(double value) {
		if (!smaInit && ema==null)
			ema = value;
		
		if (ema==null) {
			sma.add(value);
			
			if (sma.size()>=length) {
				// Init ema to value of sma
				ema = sma.getValue();
			}
			return sma.getValue();
		}
		else {
			ema = alpha * value + (1-alpha) * ema;
			return ema;
		}
	}
	
	public double getValue() {
		if (ema==null)
			return sma.getValue();
		else return ema;
	}
	
	public int size() {
		if (ema==null)
			return sma.size();
		else return length;
	}
	
	public void clear() {
		sma.clear();
		ema = null;
	}
}
