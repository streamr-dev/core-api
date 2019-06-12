package com.unifina.utils;

import java.util.AbstractQueue;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

// From https://github.com/gakesson/ConcurrencyUtils/blob/c5794c5b7c0ada763549cadfbbbd345713ace79a/BoundedPriorityBlockingQueue.java

/**
 * A bounded {@link BlockingQueue} that uses the same ordering rules as {@link PriorityBlockingQueue} and supplies
 * blocking operations similar to {@link ArrayBlockingQueue}. This class does not permit {@code null} elements. A priority queue relying on
 * {@link Comparable} natural ordering also does not permit insertion of non-comparable objects (doing so results in {@code ClassCastException}).
 * However, relying on object natural order is not necessary if upon queue construction a {@link Comparator} is provided which is used to order the
 * priority queue.<br/>
 * <br/>
 * This queue implementation is a classic bounded buffer, in which a fixed-sized array holds elements inserted by producers and extracted by
 * consumers in priority order. Once created, the capacity cannot be increased. Attempts to e.g. {@link #put(Object)} an element into a full queue
 * will result in the operation blocking; attempts to e.g. {@link #take()} an element from an empty queue will similarly block.<br/>
 * <br/>
 * This class supports an optional fairness policy for ordering waiting producer and consumer threads. By default, this ordering is not guaranteed.
 * However, a queue constructed with fairness set to true grants threads access in FIFO order. Fairness generally decreases throughput but reduces
 * variability and avoids thread starvation.<br/>
 * <br/>
 * This class and its iterator implement all of the methods of the {@link Collection} and {@link Iterator} interfaces. The Iterator
 * provided in method {@link #iterator()} is NOT guaranteed to traverse the elements of the {@link BoundedPriorityBlockingQueue} in any
 * particular order. Also, method {@code #drainTo} can be used to remove some or all elements in priority order and place them in another collection.<br/>
 * <br/>
 * Operations on this class make no guarantees about the ordering of elements with equal priority. If this behavior is desired it is required to
 * enforce an ordering, by e.g. defining custom classes or comparators that use a secondary key to break ties in primary priority values.<br/>
 * <br/>
 *
 * @param <E> The type of elements held in this {@link BoundedPriorityBlockingQueue}
 * @author Gustav Akesson - gustav.r.akesson@gmail.com
 * @see {@link PriorityBlockingQueue}
 * @see {@link ArrayBlockingQueue}
 */
public class BoundedPriorityBlockingQueue<E> extends AbstractQueue<E> implements BlockingQueue<E> {
	/**
	 * Represents tree root's array index.
	 */
	private static final int TREE_ROOT_INDEX = 0;

	/**
	 * Represents a non-value of an array index.
	 */
	private static final int NO_INDEX = -1;

	/**
	 * Priority queue represented as a balanced binary heap: the two children of queue[n] are queue[2*n+1] and queue[2*(n+1)]. The priority queue is
	 * ordered by comparator, or by the elements' natural ordering, if comparator is null: For each node n in the heap and each descendant d of n, n
	 * <= d. The element with the lowest value is in queue[{@link #TREE_ROOT_INDEX}], assuming the queue is nonempty.
	 */
	private final Object[] myQueue;

	/**
	 * The number of elements in the priority queue.
	 */
	private int mySize;

	/**
	 * The comparator, or null if priority queue uses elements' natural ordering.
	 */
	private final Comparator<? super E> myComparator;

	/**
	 * Lock used for mutual exclusion.
	 */
	private final ReentrantLock myLock;

	/**
	 * Condition for blocking when empty.
	 */
	private final Condition myNotEmpty;

	/**
	 * Condition for blocking when full.
	 */
	private final Condition myNotFull;

	/**
	 * Creates a {@code BoundedPriorityBlockingQueue} with the default access policy and the specified capacity that orders its elements according to
	 * their {@link Comparable} natural ordering.
	 *
	 * @param capacity The capacity for this priority queue.
	 * @throws IllegalArgumentException If {@code capacity} is less than 1.
	 */
	public BoundedPriorityBlockingQueue(int capacity) {
		this(capacity, null, false);
	}

	/**
	 * Creates a {@code BoundedPriorityBlockingQueue} with the specified access policy and the capacity that orders its elements according to their
	 * {@link Comparable} natural ordering.
	 *
	 * @param capacity The capacity for this priority queue.
	 * @param fair     If {@code true} then queue accesses for threads blocked on insertion or removal, are processed in FIFO order; if {@code false} the
	 *                 access order is unspecified.
	 * @throws IllegalArgumentException If {@code capacity} is less than 1.
	 */
	public BoundedPriorityBlockingQueue(int capacity, boolean fair) {
		this(capacity, null, fair);
	}

	/**
	 * Creates a {@code BoundedPriorityBlockingQueue} with the default access policy and the specified capacity that orders its elements according to
	 * the specified {@link Comparator}.
	 *
	 * @param capacity   The capacity for this priority queue.
	 * @param comparator The {@link Comparator} that will be used to order this priority queue. If {@code null}, the {@link Comparable} natural ordering of
	 *                   the elements will be used.
	 * @throws IllegalArgumentException If {@code capacity} is less than 1.
	 */
	public BoundedPriorityBlockingQueue(int capacity, Comparator<? super E> comparator) {
		this(capacity, comparator, false);
	}

	/**
	 * Creates an {@link BoundedPriorityBlockingQueue} with the provided capacity, the specified access policy and initially containing the elements
	 * of the provided collection.
	 *
	 * @param capacity   The capacity of this queue.
	 * @param fair       If {@code true} then queue accesses for threads blocked on insertion or removal, are processed in FIFO order; if {@code false} the
	 *                   access order is unspecified.
	 * @param collection The collection of elements to initially contain.
	 * @throws IllegalArgumentException If {@code capacity} is less than 1, or less than the provided collection's size.
	 * @throws NullPointerException     If the specified collection or any of its elements are null.
	 */
	public BoundedPriorityBlockingQueue(int capacity, boolean fair, Collection<? extends E> collection) {
		this(capacity, fair);

		if (capacity < collection.size()) {
			throw new IllegalArgumentException("Capacity must be greater than collection size");
		}

		for (E element : collection) {
			add(element);
		}
	}

	/**
	 * Creates a {@code BoundedPriorityBlockingQueue} with the specified access policy and the capacity that orders its elements according to the
	 * specified comparator.
	 *
	 * @param capacity   The capacity for this priority queue.
	 * @param comparator The comparator that will be used to order this priority queue. If {@code null}, the {@link Comparable} natural ordering of the
	 *                   elements will be used.
	 * @param fair       If {@code true} then queue accesses for threads blocked on insertion or removal, are processed in FIFO order; if {@code false} the
	 *                   access order is unspecified.
	 * @throws IllegalArgumentException If {@code capacity} is less than 1.
	 */
	public BoundedPriorityBlockingQueue(int capacity, Comparator<? super E> comparator, boolean fair) {
		if (capacity < 1) {
			throw new IllegalArgumentException("Capacity must be greater than 0");
		}

		myLock = new ReentrantLock(fair);
		myNotEmpty = myLock.newCondition();
		myNotFull = myLock.newCondition();
		myComparator = comparator;
		myQueue = new Object[capacity];
	}

	/**
	 * Inserts the specified element into this queue if it is possible to do so immediately without exceeding the queue's capacity, returning
	 * {@code true} upon success and throwing an {@code IllegalStateException} if this queue is full.
	 *
	 * @param element The element to add.
	 * @return {@code true}, as specified by {@link Collection#add}.
	 * @throws IllegalStateException If this queue is full.
	 * @throws NullPointerException  If the specified element is null.
	 */
	@Override
	public boolean add(E element) {
		return super.add(element);
	}

	/**
	 * Retrieves and removes the head of this queue, waiting if necessary until an element becomes available.
	 *
	 * @return The head of this queue.
	 * @throws InterruptedException If thread is interrupted while waiting for an element to become available.
	 */
	@Override
	public E take() throws InterruptedException {
		myLock.lockInterruptibly();

		try {
			try {
				while (mySize == 0) {
					myNotEmpty.await();
				}
			} catch (InterruptedException ie) {
				myNotEmpty.signal();
				throw ie;
			}

			return extractElement();
		} finally {
			myLock.unlock();
		}
	}

	/**
	 * Inserts the specified element into this queue, waiting for space to become available if the queue is full.
	 *
	 * @param element The element to add.
	 * @throws InterruptedException If thread is interrupted while waiting for insertion.
	 * @throws NullPointerException If the specified element is null.
	 */
	@Override
	public void put(E element) throws InterruptedException {
		checkNotNull(element);
		myLock.lockInterruptibly();

		try {
			try {
				while (mySize == myQueue.length) {
					myNotFull.await();
				}
			} catch (InterruptedException ie) {
				myNotFull.signal();
				throw ie;
			}

			insertElement(element);
		} finally {
			myLock.unlock();
		}
	}

	/**
	 * Inserts the specified element into this queue if it is possible to do so immediately without exceeding the queue's capacity,
	 * returning {@code true} upon success and {@code false} if this queue is full. This method is generally preferable to {@link #add}, which
	 * can fail to insert an element only by throwing an exception.
	 *
	 * @param element The element to insert.
	 * @return {@code true} upon successful insertion, otherwise {@code false}.
	 * @throws NullPointerException If the specified element is null.
	 */
	@Override
	public boolean offer(E element) {
		checkNotNull(element);
		boolean inserted = false;
		myLock.lock();

		try {
			if (mySize < myQueue.length) {
				insertElement(element);
				inserted = true;
			}
		} finally {
			myLock.unlock();
		}

		return inserted;
	}

	/**
	 * Inserts the specified element into this queue, waiting up to the specified wait time for space to become available if the queue is full.
	 *
	 * @param element The element to insert.
	 * @param timeout How long to wait before giving up, in units of {@link TimeUnit}.
	 * @param unit    A {@link TimeUnit} determining how to interpret the timeout parameter.
	 * @return {@code true} upon successful insertion, otherwise {@code false}.
	 * @throws InterruptedException If thread is interrupted while waiting for insertion.
	 * @throws NullPointerException If the specified element is null.
	 */
	@Override
	public boolean offer(E element, long timeout, TimeUnit unit) throws InterruptedException {
		checkNotNull(element);
		long nanos = unit.toNanos(timeout);
		myLock.lockInterruptibly();

		try {
			for (; ; ) {
				if (mySize < myQueue.length) {
					insertElement(element);
					return true;
				} else if (nanos <= 0) {
					return false;
				}

				try {
					nanos = myNotFull.awaitNanos(nanos);
				} catch (InterruptedException ie) {
					myNotFull.signal();
					throw ie;
				}
			}
		} finally {
			myLock.unlock();
		}
	}

	/**
	 * Retrieves and removes the head of this queue, or returns null if this queue is empty.
	 *
	 * @return The head of this queue, or null if this queue is empty.
	 */
	@Override
	public E poll() {
		myLock.lock();

		try {
			return extractElement();
		} finally {
			myLock.unlock();
		}
	}

	/**
	 * Retrieves and removes the head of this queue, waiting up to the specified wait time if necessary for an element to become available.
	 *
	 * @param timeout How long to wait before giving up, in units of {@link TimeUnit}.
	 * @param unit    A {@link TimeUnit} determining how to interpret the timeout parameter.
	 * @return The head of this queue, or null if the specified waiting time elapses before an element is available
	 * @throws InterruptedException If thread is interrupted while waiting for an element to become available.
	 */
	@Override
	public E poll(long timeout, TimeUnit unit) throws InterruptedException {
		long nanos = unit.toNanos(timeout);
		myLock.lockInterruptibly();

		try {
			for (; ; ) {
				if (mySize > 0) {
					return extractElement();
				} else if (nanos <= 0) {
					return null;
				}

				try {
					nanos = myNotEmpty.awaitNanos(nanos);
				} catch (InterruptedException ie) {
					myNotEmpty.signal();
					throw ie;
				}
			}
		} finally {
			myLock.unlock();
		}
	}

	/**
	 * Retrieves, but does not remove, the head of this queue, or returns null if this queue is empty.
	 *
	 * @return The head of this queue, or null if this queue is empty.
	 */
	@Override
	public E peek() {
		myLock.lock();

		try {
			return mySize > 0 ? (E) myQueue[TREE_ROOT_INDEX] : null;
		} finally {
			myLock.unlock();
		}
	}

	/**
	 * Returns the number of elements currently in this queue.
	 *
	 * @return The number of elements in this queue
	 */
	@Override
	public int size() {
		myLock.lock();

		try {
			return mySize;
		} finally {
			myLock.unlock();
		}
	}

	/**
	 * Returns the number of additional elements that this queue can ideally (in the absence of memory or resource constraints) accept without
	 * blocking. This is always equal to the capacity of this queue minus the current {@link #size()} of this queue.<br/>
	 * <br/>
	 * Note that you cannot always tell if an attempt to insert an element will succeed by inspecting {@link #remainingCapacity()} because it
	 * may be the case that another thread is about to insert or remove an element.
	 *
	 * @return The number of additional elements that this queue can ideally accept without blocking.
	 */
	@Override
	public int remainingCapacity() {
		myLock.lock();

		try {
			return myQueue.length - mySize;
		} finally {
			myLock.unlock();
		}
	}

	/**
	 * Removes a single instance of the specified element from this queue (if it is present). More formally, removes an element {@code e} such that
	 * {@code o.equals(e)}, if this queue contains such an element. Returns {@code true} if this queue contained the specified element (or
	 * equivalently, if this queue changed as a result of the call).<br/>
	 * <br/>
	 * Removal of interior elements in priority based queues is an intrinsically slow and disruptive operation, so should be undertaken only in
	 * exceptional circumstances, ideally only when the queue is known not to be accessible by other threads.
	 *
	 * @param object Element to be removed from this queue (if present).
	 * @return {@code true} if this queue changed as a result of the call.
	 */
	@Override
	public boolean remove(Object object) {
		boolean removed = false;
		myLock.lock();

		try {
			int i = indexOf(object);

			if (i != NO_INDEX) {
				removeAt(i);
				removed = true;
			}
		} finally {
			myLock.unlock();
		}
		return removed;
	}

	/**
	 * Returns {@code true} if this queue contains the specified element. More formally, returns {@code true} if and only if this queue contains at
	 * least one element {@code e} such that {@code o.equals(e)}.
	 *
	 * @param object Object to be checked for containment in this queue.
	 * @return {@code true} if this queue contains the specified element.
	 */
	@Override
	public boolean contains(Object object) {
		myLock.lock();

		try {
			return indexOf(object) != NO_INDEX;
		} finally {
			myLock.unlock();
		}
	}

	/**
	 * Removes all available elements from this queue and adds them to the given collection. This operation may be more efficient than repeatedly
	 * polling this queue. A failure encountered while attempting to add elements to the provided collection may result in elements being in neither,
	 * either or both collections when the associated exception is thrown. Attempts to drain a queue to itself result in
	 * {@link IllegalArgumentException}. Further, the behavior of this operation is undefined if the provided collection is modified while the
	 * operation is in progress.
	 *
	 * @param collectionToDrainTo The collection to transfer elements into.
	 * @return The number of elements transferred.
	 * @throws UnsupportedOperationException If addition of elements is not supported by the specified collection.
	 * @throws ClassCastException            If the class of an element of this queue prevents it from being added to the specified collection.
	 * @throws NullPointerException          If the specified collection is null.
	 * @throws IllegalArgumentException      If the specified collection is this queue, or some property of an element of this queue prevents it from being added to the
	 *                                       specified collection.
	 */
	@Override
	public int drainTo(Collection<? super E> collectionToDrainTo) {
		checkNotNull(collectionToDrainTo);
		checkNotSame(collectionToDrainTo, this);
		int numberOfDrainedElements = 0;
		myLock.lock();

		try {
			E drainedElement = extractElementWithoutSignalingWaitingThread();

			while (drainedElement != null) {
				collectionToDrainTo.add(drainedElement);
				++numberOfDrainedElements;
				drainedElement = extractElementWithoutSignalingWaitingThread();
			}

			if (numberOfDrainedElements > 0) {
				myNotFull.signalAll();
			}
		} finally {
			myLock.unlock();
		}

		return numberOfDrainedElements;
	}

	/**
	 * Removes at most the provided number of available elements from this queue and adds them to the provided collection. A failure encountered while
	 * attempting to add elements to the provided collection may result in elements being in neither, either or both collections when the associated
	 * exception is thrown. Attempts to drain a queue to itself result in {@link IllegalArgumentException}. Further, the behavior of this operation
	 * is undefined if the provided collection is modified while the operation is in progress.
	 *
	 * @param collectionToDrainTo The collection to transfer elements into.
	 * @param maxElementsToDrain  The maximum number of elements to transfer.
	 * @return The number of elements transferred.
	 * @throws UnsupportedOperationException If addition of elements is not supported by the specified collection.
	 * @throws ClassCastException            If the class of an element of this queue prevents it from being added to the specified collection.
	 * @throws NullPointerException          If the specified collection is null.
	 * @throws IllegalArgumentException      If the specified collection is this queue, or some property of an element of this queue prevents it from being added to the
	 *                                       specified collection.
	 */
	@Override
	public int drainTo(Collection<? super E> collectionToDrainTo, int maxElementsToDrain) {
		checkNotNull(collectionToDrainTo);
		checkNotSame(collectionToDrainTo, this);
		int numberOfDrainedElements = 0;
		myLock.lock();

		try {
			E drainedElement;
			while (numberOfDrainedElements < maxElementsToDrain && (drainedElement = extractElementWithoutSignalingWaitingThread()) != null) {
				collectionToDrainTo.add(drainedElement);
				++numberOfDrainedElements;
			}

			if (numberOfDrainedElements > 0) {
				myNotFull.signalAll();
			}
		} finally {
			myLock.unlock();
		}

		return numberOfDrainedElements;
	}

	/**
	 * Atomically removes all of the elements from this queue. The queue will be empty after this call returns.
	 */
	@Override
	public void clear() {
		myLock.lock();

		try {
			for (int i = 0; i < mySize; i++) {
				myQueue[i] = null;
			}

			mySize = 0;
			myNotFull.signalAll();
		} finally {
			myLock.unlock();
		}
	}

	/**
	 * Returns an array containing all of the elements in this queue. The returned array elements are in no particular order.<br/>
	 * <br/>
	 * The returned array will be "safe" in that no references to it are maintained by this queue. (In other words, this method must allocate a new
	 * array). The caller is thus free to modify the returned array without impacting this queue.
	 *
	 * @return An array containing all of the elements in this queue (in no particular order).
	 */
	@Override
	public Object[] toArray() {
		myLock.lock();

		try {
			return Arrays.copyOf(myQueue, mySize);
		} finally {
			myLock.unlock();
		}
	}

	/**
	 * Returns an array containing all of the elements in this queue; the runtime type of the returned array is that of the specified array. The
	 * returned array elements are in no particular order. If the queue fits in the specified array, it is returned therein. Otherwise, a new array is
	 * allocated with the runtime type of the specified array and the size of this queue. If this queue fits in the specified array with room to spare
	 * (i.e., the array has more elements than this queue), the element in the array immediately following the end of the queue is set to null.<br/>
	 * <br/>
	 * Note that {@code toArray(new Object[0])} is identical in function to {@code toArray()}.
	 *
	 * @param newArray The array into which the elements of the queue are to be stored, if it is big enough; otherwise, a new array of the same runtime
	 *                 type is allocated for this purpose.
	 * @return An array containing all of the elements in this queue.
	 * @throws ArrayStoreException  If the runtime type of the specified array is not a supertype of the runtime type of every element in this queue.
	 * @throws NullPointerException If the specified array is null.
	 */
	@Override
	public <T> T[] toArray(T[] newArray) {
		myLock.lock();

		try {
			if (newArray.length < mySize) {
				return (T[]) Arrays.copyOf(myQueue, mySize, newArray.getClass());
			}

			System.arraycopy(myQueue, 0, newArray, 0, mySize);

			if (newArray.length > mySize) {
				newArray[mySize] = null;
			}

			return newArray;
		} finally {
			myLock.unlock();
		}
	}

	/**
	 * Generates a {@link String} based on the underlying queue array.
	 *
	 * @return The string representation of the underlying queue array.
	 */
	@Override
	public String toString() {
		return Arrays.toString(toArray());
	}

	/**
	 * Returns an iterator over the elements in this queue. The iterator does not return the elements in any particular order.<br/>
	 * <br/>
	 * The returned iterator is a "weakly consistent" iterator that will never throw {@link ConcurrentModificationException}, and guarantees to
	 * traverse elements as they existed upon construction of the iterator, and may (but is not guaranteed to) reflect any modifications subsequent to
	 * construction.
	 *
	 * @return An {@link Iterator} over the elements in this queue.
	 */
	@Override
	public Iterator<E> iterator() {
		return new QueueIterator(toArray());
	}

	/**
	 * Verifies that the provided object is not null, and if it is a {@link NullPointerException} is thrown.
	 *
	 * @param object The object to verify.
	 */
	private static void checkNotNull(Object object) {
		if (object == null) {
			throw new NullPointerException();
		}
	}

	/**
	 * Verifies that the two provided references don't refer to the very same object, and if they do an {@link IllegalArgumentException} is thrown.
	 *
	 * @param first  The first reference to compare.
	 * @param second The second reference to compare.
	 */
	private static void checkNotSame(Object first, Object second) {
		if (first == second) {
			throw new IllegalArgumentException("Not allowed due to same object");
		}
	}

	/**
	 * Removes the element at the provided index from the queue array. <br/>
	 * Call only using mutual exclusion.<br/>
	 *
	 * @param index The queue array index to remove an element.
	 */
	private void removeAt(int index) {
		int lastElementInQueue = mySize - 1;

		if (lastElementInQueue == index) {
			myQueue[index] = null;
		} else {
			E movedElement = (E) myQueue[lastElementInQueue];
			myQueue[lastElementInQueue] = null;
			siftDownElement(index, movedElement, myQueue, lastElementInQueue, myComparator);

			if (myQueue[index] == movedElement) {
				siftUpElement(index, movedElement, myQueue, myComparator);
			}
		}

		--mySize;
		myNotFull.signal();
	}

	/**
	 * Returns the queue array index of the provided object. Evaluation is based on equality. <br/>
	 * Call only using mutual exclusion.<br/>
	 *
	 * @param object
	 * @return
	 */
	private int indexOf(Object object) {
		if (object != null) {
			for (int i = 0; i < mySize; i++) {
				if (object.equals(myQueue[i])) {
					return i;
				}
			}
		}

		return NO_INDEX;
	}

	/**
	 * Inserts the specified element into the queue.<br/>
	 * <br/>
	 * Call only using mutual exclusion.
	 *
	 * @param e
	 */
	private void insertElement(E element) {
		siftUpElement(mySize, element, myQueue, myComparator);
		++mySize;
		myNotEmpty.signal();
	}

	/**
	 * Extracts an element from the queue (if present), and if successful in doing so the method will signal an awaiting thread that an element has
	 * been removed.<br/>
	 * <br/>
	 * Call only using mutual exclusion.<br/>
	 * <br/>
	 * This method will return null in case the queue is empty.
	 *
	 * @return The extracted element.
	 */
	private E extractElement() {
		E element = extractElementWithoutSignalingWaitingThread();

		if (element != null) {
			myNotFull.signal();
		}

		return element;
	}

	/**
	 * Extracts an element from the queue (if present). This method will NOT signal an awaiting thread that an element has been removed.<br/>
	 * <br/>
	 * Call only using mutual exclusion.<br/>
	 * <br/>
	 * This method will return null in case the queue is empty.
	 *
	 * @return The extracted element.
	 */
	private E extractElementWithoutSignalingWaitingThread() {
		E element = null;

		if (mySize > 0) {
			int lastIndexOfQueue = mySize - 1;
			element = (E) myQueue[TREE_ROOT_INDEX];
			E elementToMove = (E) myQueue[lastIndexOfQueue];
			myQueue[lastIndexOfQueue] = null;
			siftDownElement(0, elementToMove, myQueue, lastIndexOfQueue, myComparator);
			--mySize;
		}

		return element;
	}

	/**
	 * Inserts the provided element at the insertion index, maintaining heap invariant by promoting the element up the tree until it is greater than
	 * or equal to its parent, or is the root of the tree.<br/>
	 * <br/>
	 * This method does not guarantee any type of thread-safety - such mechanisms are up to the call site.<br/>
	 * <br/>
	 * This method is static, with heap state as arguments.
	 *
	 * @param insertionIndex The position to insert at, which might change when traversing the heap searching for the actual insertion index.
	 * @param element        The element to insert.
	 * @param heap           The heap array.
	 * @param comparator     The {@link Comparator} to use. Null value is supported, and in that case the {@link Comparable} natural ordering of the provided
	 *                       element is used.
	 */
	private static <T> void siftUpElement(int insertionIndex, T element, Object[] heap, Comparator<? super T> comparator) {
		boolean hasComparator = comparator != null;
		Comparable<? super T> comparableElement = hasComparator ? null : (Comparable<? super T>) element;

		while (insertionIndex > 0) {
			int parentIndex = (insertionIndex - 1) >>> 1;
			T parentElement = (T) heap[parentIndex];
			boolean equalOrGreaterThanZero = hasComparator
				? (comparator.compare(element, parentElement) >= 0)
				: comparableElement.compareTo(parentElement) >= 0;

			if (equalOrGreaterThanZero) {
				break;
			}

			heap[insertionIndex] = parentElement;
			insertionIndex = parentIndex;
		}

		heap[insertionIndex] = element;
	}

	/**
	 * Inserts provided element at the insertion index, maintaining heap invariant by demoting the element down the tree repeatedly until it is less
	 * than or equal to its children or is a leaf.<br/>
	 * <br/>
	 * This method does not guarantee any type of thread-safety - such mechanisms are up to the call site.<br/>
	 * <br/>
	 * This method is static, with heap state as arguments.
	 *
	 * @param insertionIndex The position to insert at, which might change when traversing the heap searching for the actual insertion index.
	 * @param element        The element to insert.
	 * @param heap           The heap array.
	 * @param heapSize       The heap size.
	 * @param comparator     The {@link Comparator} to use. Null value is supported, and in that case the {@link Comparable} natural ordering of the provided
	 *                       element is used.
	 */
	private static <T> void siftDownElement(int insertionIndex, T element, Object[] heap, int heapSize, Comparator<? super T> comparator) {
		boolean hasComparator = comparator != null;
		Comparable<? super T> comparableElement = hasComparator ? null : (Comparable<? super T>) element;
		int half = heapSize >>> 1;

		while (insertionIndex < half) {
			int childIndex = (insertionIndex << 1) + 1;
			Object childElement = heap[childIndex];
			int rightIndex = childIndex + 1;

			if (rightIndex < heapSize) {
				boolean greaterThanZero = hasComparator
					? comparator.compare((T) childElement, (T) heap[rightIndex]) > 0
					: ((Comparable<? super T>) childElement).compareTo((T) heap[rightIndex]) > 0;

				if (greaterThanZero) {
					childElement = heap[childIndex = rightIndex];
				}
			}

			boolean equalOrLessThanZero = hasComparator
				? comparator.compare(element, (T) childElement) <= 0
				: comparableElement.compareTo((T) childElement) <= 0;

			if (equalOrLessThanZero) {
				break;
			}

			heap[insertionIndex] = childElement;
			insertionIndex = childIndex;
		}

		heap[insertionIndex] = element;
	}

	/**
	 * An {@link Iterator} implementation which takes a snapshot of the underlying queue array.
	 */
	private final class QueueIterator implements Iterator<E> {
		private final Object[] myArray;
		private int myIndexToReturn;
		private int myLastReturnedIndex;

		/**
		 * Creates a new {@link QueueIterator} instance.
		 *
		 * @param array The array to iterate over.
		 */
		QueueIterator(Object[] array) {
			myLastReturnedIndex = NO_INDEX;
			myArray = array;
		}

		@Override
		public boolean hasNext() {
			return myIndexToReturn < myArray.length;
		}

		@Override
		public E next() {
			if (myIndexToReturn >= myArray.length) {
				throw new NoSuchElementException();
			}

			myLastReturnedIndex = myIndexToReturn;
			return (E) myArray[myIndexToReturn++];
		}

		@Override
		public void remove() {
			if (myLastReturnedIndex == NO_INDEX) {
				throw new IllegalStateException();
			}

			removeElementByIdentity(myArray[myLastReturnedIndex]);
			myLastReturnedIndex = NO_INDEX;
		}

		/**
		 * Identity-based removal of the provided object. This method will be executed using mutual exclusion.
		 */
		private void removeElementByIdentity(Object object) {
			myLock.lock();

			try {
				for (int i = 0; i < mySize; i++) {
					if (object == myQueue[i]) {
						removeAt(i);
						break;
					}
				}
			} finally {
				myLock.unlock();
			}
		}
	}
}
