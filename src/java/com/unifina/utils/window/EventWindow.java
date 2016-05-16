package com.unifina.utils.window;

import java.util.Iterator;

/**
 * A Window whose length is defined by number of events in the window
 */
public class EventWindow<T> extends AbstractWindow<T> {

	public EventWindow(int length, WindowListener listener) {
		super(length, listener);
	}

	@Override
	protected boolean hasExtraValues() {
		return values.size() > length;
	}

}
