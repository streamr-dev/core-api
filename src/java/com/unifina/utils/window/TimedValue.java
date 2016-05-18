package com.unifina.utils.window;

import java.io.Serializable;
import java.util.Date;

/**
 * A wrapper for a value of type T and its associated timestamp
 */
public class TimedValue<T> implements Serializable {
	public final T value;
	public final Date time;

	public TimedValue(T value, Date time) {
		this.value = value;
		this.time = time;
	}
}
