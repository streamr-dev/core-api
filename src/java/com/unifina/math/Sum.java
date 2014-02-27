package com.unifina.math;

import java.util.LinkedList;
import java.util.List;

public class Sum implements IWindowedOperation {

	int length;
	double sum = 0;
	List<Double> values;
	
	public Sum(int length) {
		this.length = length;
		values = new LinkedList<Double>();
	}
	
	public int getLength() {
		return length;
	}

	public int size() {
		return values.size();
	}
	
	public void setLength(int length) {
		this.length = length;
		
		while (values.size() > length) {
			double removedVal = values.remove(0);
			sum -= removedVal;
		}
	}

	public double add(double p) {
		values.add(p);
		sum += p;

		while (values.size() > length) {
			double removedVal = values.remove(0);
			sum -= removedVal;
		}
		
		return getValue();
	}
	
	public double getValue() {
		if (values.size()>0)
			return sum;
		else return 0;
	}
	
	public void clear() {
		sum = 0;
		values.clear();
	}
	
}
