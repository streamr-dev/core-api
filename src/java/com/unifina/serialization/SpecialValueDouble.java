package com.unifina.serialization;

/**
 * Used to serialize / deserialize special Double types, i.e., NaN, POSITIVE_INFINITY, and NEGATIVE_INFINITY
 */
public class SpecialValueDouble {
	String d = null;

	public SpecialValueDouble(Double d) {
		this.d = d.toString();
	}
}
