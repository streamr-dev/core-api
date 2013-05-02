package com.unifina.feed;

public interface Catchup<T> {
	/**
	 * Returns the next object stored or null if there is nont
	 * @return
	 */
	public T getNext();
	
	/**
	 * Returns the index of the next object stored, retrievable by getNext().
	 * There is no guarantee that the Catchup actually contains an object for
	 * this index, ie. getNext() may return null.
	 * @return
	 */
	public int getNextCounter();
}
