package com.unifina.feed.util;

import com.google.common.collect.Iterators;
import org.apache.log4j.Logger;

import java.io.Closeable;
import java.io.IOException;
import java.util.Comparator;
import java.util.Iterator;

public class MergingIterator<T> implements Iterator<T>, Closeable {

	private static final Logger log = Logger.getLogger(MergingIterator.class);

	private final Iterable<? extends Iterator<? extends T>> iterators;
	private final Iterator<T> iterator;

	public MergingIterator(Iterable<? extends Iterator<? extends T>> iterators, Comparator<? super T> comparator) {
		this.iterators = iterators;
		iterator = Iterators.mergeSorted(iterators, comparator);
	}

	@Override
	public boolean hasNext() {
		return iterator.hasNext();
	}

	@Override
	public T next() {
		return iterator.next();
	}

	@Override
	public void remove() {
		iterator.remove();
	}

	@Override
	public void close() {
		for (Iterator<? extends T> i : iterators) {
			if (i instanceof Closeable) {
				try {
					((Closeable) i).close();
				} catch (IOException e) {
					log.error("Failed to close iterator (ignoring exception): "+i, e);
				}
			}
		}
	}
}
