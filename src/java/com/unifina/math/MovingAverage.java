package com.unifina.math;

import java.util.LinkedList;
import java.util.List;

public class MovingAverage {
	int maLength;
	
	double maSum = 0;
	List<Double> maValues;
	double ma = 0;
	
	public MovingAverage(int maLength) {
		this.maLength = maLength;
		maValues = new LinkedList<Double>();
	}
	
	public int getLength() {
		return maLength;
	}

	public int size() {
		return maValues.size();
	}
	
	public void setLength(int maLength) {
		this.maLength = maLength;
		
		while (maValues.size() > maLength) {
			double removedVal = maValues.remove(0);
			maSum -= removedVal;
		}
	}

	public double add(double p) {
		maValues.add(p);
		maSum += p;

		while (maValues.size() > maLength) {
			double removedVal = maValues.remove(0);
			maSum -= removedVal;
		}
		
		if (maValues.size()>0)
			ma = maSum / maValues.size();
		else ma = 0;
		
		return ma;
	}
	
	public double getValue() {
		return ma;
	}
	
	public void clear() {
		maSum = 0;
		maValues.clear();
		ma = 0;
	}
	
}
