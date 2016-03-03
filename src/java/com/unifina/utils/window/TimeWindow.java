package com.unifina.utils.window;

import java.util.Date;

/**
 * A Window whose length is defined by number of events in the window
 */
public class TimeWindow<T> extends AbstractWindow<TimedValue<T>> {

	private Date time;

	public TimeWindow(int length, final WindowListener<T> originalListener) {
		super(length, null);

		// Wrap the item into a TimedValue
		this.listener = new WindowListener<TimedValue<T>>() {
			@Override
			public void onAdd(TimedValue<T> item) {
				originalListener.onAdd(item.value);
			}

			@Override
			public void onRemove(TimedValue<T> item) {
				originalListener.onRemove(item.value);
			}

			@Override
			public void onClear() {
				originalListener.onClear();
			}
		};
	}

	public void add(T item, Date time) {
		this.time = time;
		add(new TimedValue<>(item, time));
	}

	/**
	 * Needs to be updated with current time for the window to work
	 * @param time
     */
	public void setTime(Date time) {
		this.time = time;
		purgeExtraValues();
	}

	public Date getTime() {
		return time;
	}

	@Override
	protected boolean hasExtraValues() {
		return this.time.getTime() - length*1000 >= values.peekFirst().time.getTime();
	}
}
