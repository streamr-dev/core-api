package com.unifina.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

/**
 * Partial implementation of a bounded priority queue. The implementation extends a PriorityBlockingQueue,
 * adding blocking behavior with the help of a Semaphore.
 *
 * Rolled own implementation because none were available in commonly used libraries.
 *
 * Also tried this, but it was buggy and caused segfaults: https://github.com/gakesson/ConcurrencyUtils/blob/c5794c5b7c0ada763549cadfbbbd345713ace79a/BoundedPriorityBlockingQueue.java
 *
 * NOTE: only offer(E, long, TimeUnit) and poll(long, TimeUnit) are implemented properly.
 * DO NOT USE other methods to manipulate the queue!
 */
public class BoundedPriorityBlockingQueue<E> extends PriorityBlockingQueue<E> {

	// This should always hold: semaphore.availablePermits() == maxSize - this.size()
	private final Semaphore semaphore;

	public BoundedPriorityBlockingQueue(int maxSize) {
		super(maxSize);
		this.semaphore = new Semaphore(maxSize, false);
	}

	@Override
	public synchronized boolean offer(E e, long timeout, @NotNull TimeUnit unit) {
		try {
			if (semaphore.tryAcquire(timeout, unit)) {
				super.offer(e);
				return true;
			} else {
				return false;
			}
		} catch (InterruptedException ex) {
			return false;
		}
	}

	@Nullable
	@Override
	public E poll(long timeout, @NotNull TimeUnit unit) throws InterruptedException {
		E result = super.poll(timeout, unit);
		if (result != null) {
			semaphore.release();
		}
		return result;
	}

	@Override
	public int remainingCapacity() {
		return semaphore.availablePermits();
	}

	// Throw on unimplemented methods to avoid accidental misuse

	@Override
	public boolean add(E e) {
		throw new RuntimeException("Not implemented!");
	}

	@Override
	public boolean remove(Object o) {
		throw new RuntimeException("Not implemented!");
	}

	@Override
	public boolean offer(E e) {
		throw new RuntimeException("Not implemented!");
	}

	@Override
	public E poll() {
		throw new RuntimeException("Not implemented!");
	}

	@Override
	public void put(E e) {
		throw new RuntimeException("Not implemented!");
	}

	@Override
	public E take() throws InterruptedException {
		throw new RuntimeException("Not implemented!");
	}

	@Override
	public int drainTo(Collection<? super E> c) {
		throw new RuntimeException("Not implemented!");
	}

	@Override
	public int drainTo(Collection<? super E> c, int maxElements) {
		throw new RuntimeException("Not implemented!");
	}

	@Override
	public void clear() {
		throw new RuntimeException("Not implemented!");
	}

	@Override
	public E remove() {
		throw new RuntimeException("Not implemented!");
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		throw new RuntimeException("Not implemented!");
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		throw new RuntimeException("Not implemented!");
	}

	@Override
	public boolean removeIf(Predicate<? super E> filter) {
		throw new RuntimeException("Not implemented!");
	}
}
