package com.unifina.feed.util;

import org.apache.log4j.Logger;

import java.io.Closeable;
import java.io.IOException;
import java.util.Iterator;

public abstract class ChainingIterator<T> implements Iterator<T>, Closeable {

	private Iterator<T> currentIterator;

	private static final Logger log = Logger.getLogger(ChainingIterator.class);

	@Override
	public boolean hasNext() {
		ensureIterator();
		return currentIterator.hasNext();
	}

	@Override
	public T next() {
		ensureIterator();
		return currentIterator.next();
	}

	@Override
	public void remove() {

	}

	@Override
	public void close() {
		tryCloseIterator(currentIterator);
	}

	/**
	 * Returns true if currentIterator has a next value, false otherwise.
	 * Replaces currentIterator with the next one in the chain if it is empty.
     */
	private boolean ensureIterator() {
		if (currentIterator == null) {
			currentIterator = nextIterator();
			return currentIterator.hasNext();
		} else if (!currentIterator.hasNext()) {
			tryCloseIterator(currentIterator);
			currentIterator = nextIterator();
		}

		return currentIterator.hasNext();
	}

	private void tryCloseIterator(Iterator<T> i) {
		if (i != null && i instanceof Closeable) {
			try {
				((Closeable) i).close();
			} catch (IOException e) {
				log.warn("Failed to close iterator: "+i);
			}
		}
	}

	/**
	 * Should return the next iterator, or null if there is none
     */
	protected abstract Iterator<T> nextIterator();
}
