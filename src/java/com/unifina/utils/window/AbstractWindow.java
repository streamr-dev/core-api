package com.unifina.utils.window;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Iterator;

/**
 * An abstraction of a sliding window that supports adding new objects of type T and removing old ones.
 * Subclasses get to determine if any values need to be removed. An integer field called "length" is
 * always present, but its interpretation is done by the subclass. A length of 0 always means
 * an infinite window, however.
 *
 * Methods on the WindowListener are called when items are added or removed or when the window is cleared.
 */
public abstract class AbstractWindow<T> implements Serializable, Iterable<T> {
	protected WindowListener listener;
	protected int length;
	protected int size = 0;
	protected ArrayDeque<T> values;

	public AbstractWindow(int length, @Nullable WindowListener listener) {
		if (length < 0)
			throw new IllegalArgumentException("Invalid window length: "+length);

		this.length = length;
		this.listener = listener;
		values = new ArrayDeque<T>(length);
	}

	public int getLength() {
		return length;
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

	public int getSize() {
		return size;
	}

	public void add(T item) {
		// Don't keep values in memory if length is 0 (infinite)
		if (length > 0)
			values.add(item);

		// Keep track of size. Equals values.size() for finite windows, but for values is empty for infinite windows.
		size++;

		if (listener != null) {
			listener.onAdd(item);
		}

		purgeExtraValues();
	}

	/**
	 * Removes extra values from the window.
     */
	protected void purgeExtraValues() {
		if (length > 0) {
			while (hasExtraValues()) {
				T removedItem = values.removeFirst();
				size--;

				if (listener != null) {
					listener.onRemove(removedItem);
				}
			}
		}
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

		if (listener != null) {
			listener.onClear();
		}
	}

	@Override
	public Iterator<T> iterator() {
		return values.iterator();
	}

}
