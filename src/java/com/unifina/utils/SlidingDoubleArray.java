package com.unifina.utils;

import java.io.Serializable;

@Deprecated
public class SlidingDoubleArray implements Serializable {
	
	int maxSize;
	
	double[] values;
	int index = 0;
	
	public SlidingDoubleArray(int size) {
		this.maxSize = size;
		values = new double[size];
	}
	
	public void add(double value) {
		if (index < maxSize)
			values[index++] = value;
		else {
			// Shift all values to the left by one
			System.arraycopy(values, 1, values, 0, maxSize-1);
			// Overwrite last value
			values[maxSize-1] = value;
		}
	}
	
	public double[] getValues() {
		// If the window is not yet full, must make a copy
		if (index==0)
			return new double[0];
		else if (index < maxSize) {
			double[] result = new double[index];
			System.arraycopy(values, 0, result, 0, index);
			return result;
		}
		else return values;
	}
	
	public int maxSize() {
		return maxSize;
	}
	
	public int size() {
		return index;
	}
	
	public boolean isFull() {
		return maxSize == index;
	}
	
	public void changeSize(int size) {
		if (this.maxSize==size)
			return;
		// Resize down
		else if (size < this.maxSize) {
			double[] newValues = new double[size];
			System.arraycopy(values,Math.min(this.maxSize,index)-Math.min(size,index),newValues,0,size);
			values = newValues;
			this.maxSize = size;
			this.index = Math.min(index, size);
		}
		// Resize up
		else {
			double[] newValues = new double[size];
			System.arraycopy(values,0,newValues,0,this.maxSize);
			values = newValues;
			this.maxSize = size;
			// index stays the same
		}
	}
	
	public void clear() {
		index = 0;
		values = new double[maxSize]; // not necessary but just in case
	}
	
}
