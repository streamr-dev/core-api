package com.unifina.utils;

public class SlidingDoubleArray {
	
	int size;
	
	double[] values;
	int index = 0;
	
	public SlidingDoubleArray(int size) {
		this.size = size;
		values = new double[size];
	}
	
	public void add(double value) {
		if (index < size)
			values[index++] = value;
		else {
			// Shift all values to the left by one
			System.arraycopy(values, 1, values, 0, size-1);
			// Overwrite last value
			values[size-1] = value;
		}
	}
	
	public double[] getValues() {
		return values;
	}
	
	public int maxSize() {
		return size;
	}
	
	public int size() {
		return index;
	}
	
	public boolean isFull() {
		return size == index;
	}
	
	public void changeSize(int size) {
		if (this.size==size)
			return;
		else if (size < this.size) {
			double[] newValues = new double[size];
			System.arraycopy(values,this.size-size,newValues,0,size);
			this.size = size;
		}
		else {
			double[] newValues = new double[size];
			System.arraycopy(values,0,newValues,size-this.size,size);
			this.size = size;
		}
	}
	
}
