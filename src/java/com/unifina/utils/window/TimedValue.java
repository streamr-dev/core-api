package com.unifina.utils.window;

import java.util.Date;

/**
 * Created by henripihkala on 03/03/16.
 */
public class TimedValue<T> {
	public final T value;
	public final Date time;

	public TimedValue(T value, Date time) {
		this.value = value;
		this.time = time;
	}
}
