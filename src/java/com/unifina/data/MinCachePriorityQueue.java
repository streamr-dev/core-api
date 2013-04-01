package com.unifina.data;

import java.util.PriorityQueue;

/**
 * Attempted to create a PriorityQueue extension where inserting and subsequently
 * polling a minimum value is considerably faster than in the original implementation.
 * This was supposed to work by avoiding/delaying an insert into the heap if the value is a 
 * new minimum. The speed improvement was unfortunately found to be negligible.
 * 
 * @author Henri
 *
 * @param <E>
 */
public class MinCachePriorityQueue<E> extends PriorityQueue<E> {
	Comparable<E> cachedMin = null;
	
//	int offers = 0;
//	int first = 0;
//	int second = 0;
//	int third = 0;
//	int fourth = 0;
//	
//	int pollCached = 0;
//	int pollHeap = 0;
	
	@Override
	public boolean offer(E e) {
//		offers++;
		
		Comparable<E> ce = (Comparable<E>) e;
		
		// New cached min
		if (cachedMin!=null) {
			if (cachedMin.compareTo(e)<0) {
//				first++;
				super.offer((E)cachedMin);
				cachedMin = ce;
			}
			else {
//				second++;
				super.offer(e);
			}
		}
		else {
			E heapMin = super.peek();
			if (heapMin==null || ce.compareTo(heapMin)<0) {
//				third++;
				cachedMin = ce;
			}
			else {
//				fourth++;
				super.offer(e);
			}
		}
		
		return true;
	}
	
	@Override
	public boolean isEmpty() {
		return super.isEmpty() && cachedMin==null;
	}
	
	@Override
	public E peek() {
		if (cachedMin!=null)
			return (E)cachedMin;
		else return super.peek();
	}
	
	@Override
	public E poll() {
		if (cachedMin!=null) {
//			pollCached++;
			E result = (E)cachedMin;
			cachedMin = null;
			return result;
		}
		else {
//			pollHeap++;
			return super.poll();
		}
	}
	
	@Override
	public void clear() {
		super.clear();
		cachedMin = null;
	}
	
//	public void printStats() {
//		System.out.println("Offers: "+offers);
//		System.out.println("First: "+first);
//		System.out.println("Second: "+second);
//		System.out.println("Third: "+third);
//		System.out.println("Fourth: "+fourth);
//		System.out.println("PollCached: "+pollCached);
//		System.out.println("PollHeap: "+pollHeap);
//	}
}
