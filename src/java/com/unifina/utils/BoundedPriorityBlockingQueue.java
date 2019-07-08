package com.unifina.utils;

import org.apache.log4j.Logger;
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
 * NOTE: only offer(E, long, TimeUnit) and poll(long, TimeUnit) are implemented properly.
 * DO NOT USE other methods to manipulate the queue!
 */
public class BoundedPriorityBlockingQueue<E> extends PriorityBlockingQueue<E> {

	private static final Logger log = Logger.getLogger(BoundedPriorityBlockingQueue.class);

	// This should always hold: semaphore.availablePermits() == maxSize - this.size()
	private final Semaphore semaphore;

	public BoundedPriorityBlockingQueue(int maxSize) {
		super(maxSize);
		this.semaphore = new Semaphore(maxSize, false);
	}

	@Override
	public synchronized boolean offer(E e, long timeout, @NotNull TimeUnit unit) {
		try {
			if (semaphore.availablePermits() == 0) {
				log.info("queue looks full, might block");
			}
			long startTime = System.currentTimeMillis();
			if (semaphore.tryAcquire(timeout, unit)) {
				log.info("Acquiring semaphore took "+(System.currentTimeMillis()-startTime+" millis. Remaining permits: "+semaphore.availablePermits()));
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
