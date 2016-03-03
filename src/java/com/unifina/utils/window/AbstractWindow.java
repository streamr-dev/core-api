package com.unifina.utils.window;

import java.io.Serializable;
import java.util.ArrayDeque;

/**
 * Created by henripihkala on 03/03/16.
 */
public abstract class AbstractWindow<T> implements Serializable {
	protected WindowListener listener;
	protected int length;
	protected int size = 0;
	protected ArrayDeque<T> values;

	public AbstractWindow(int length, WindowListener listener) {
		if (length < 0)
			throw new IllegalArgumentException("Invalid window length: "+length);

		this.length = length;
		this.listener = listener;
		values = new ArrayDeque<T>(length);
	}

	public int getLength() {
		return length;
	}

	public int size() {
		return values.size();
	}

	public void setLength(int length) {
		if (this.length==0 && length!=0) {
			throw new IllegalArgumentException("An infinite window may not change length!");
		}
		else if (length < 0)
			throw new IllegalArgumentException("Invalid window length: "+length);

		if (this.length!=length) {
			this.length = length;

			// Don't keep values in memory if length is 0 (infinite)
			if (length == 0)
				values.clear();

			purgeExtraValues();
		}
	}

	public void add(T item) {
		// Don't keep values in memory if length is 0 (infinite)
		if (length > 0)
			values.add(item);

		size++;
		listener.onAdd(item);

		purgeExtraValues();
	}

	protected void purgeExtraValues() {
		while (length > 0 && hasExtraValues()) {
			T removedItem = values.removeFirst();
			size--;
			listener.onRemove(removedItem);
		}
	}

	public int getSize() {
		return size;
	}

	/**
	 * Should return true if the oldest value in the window should
	 * no longer be in the window.
	 * @return
     */
	protected abstract boolean hasExtraValues();

	public void clear() {
		values.clear();
		size = 0;
		listener.onClear();
	}
}
