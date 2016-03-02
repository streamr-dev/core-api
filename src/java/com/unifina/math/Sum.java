package com.unifina.math;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

public class Sum implements IWindowedOperation, Serializable {

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

		// Don't keep values in memory if length is 0 (infinite)
		if (length==0)
			values.clear();

		purgeExtraValues();
	}

	public double add(double p) {
		// Don't keep values in memory if length is 0 (infinite)
		if (length > 0)
			values.add(p);

		sum += p;

		purgeExtraValues();
		
		return getValue();
	}

	protected int purgeExtraValues() {
		int purged = 0;
		// Don't remove values if length is 0 (infinite)
		while (length > 0 && values.size() > length) {
			double removedVal = values.remove(0);
			sum -= removedVal;
			purged++;
		}
		return purged;
	}
	
	public double getValue() {
		return sum;
	}
	
	public void clear() {
		sum = 0;
		values.clear();
	}
	
}
