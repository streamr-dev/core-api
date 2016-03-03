package com.unifina.utils.window;

/**
 * Created by henripihkala on 03/03/16.
 */
public interface WindowListener<T> {
	void onAdd(T item);
	void onRemove(T item);
	void onClear();
}
