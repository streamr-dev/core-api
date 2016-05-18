package com.unifina.utils.window;

import java.io.Serializable;

/**
 * A listener that can listen for events on a window containing items of type T.
 */
public interface WindowListener<T> extends Serializable {
	void onAdd(T item);
	void onRemove(T item);
	void onClear();
}
