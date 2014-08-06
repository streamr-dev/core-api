package com.unifina.math;

/**
 * Just a helper interface to make sure there is no accidental variation
 * in method naming across classes
 * @author Henri
 *
 */
public interface IWindowedOperation {
	public int getLength();
	public int size();
	public void setLength(int length);
	public double add(double p);
	public double getValue();
	public void clear();
}
