package com.unifina.signalpath;

import com.unifina.datasource.ITimeListener;
import com.unifina.utils.window.AbstractWindow;
import com.unifina.utils.window.EventWindow;
import com.unifina.utils.window.TimeWindow;
import com.unifina.utils.window.WindowListener;

import java.util.Date;
import java.util.Map;

/**
 * Abstraction of a module that collects values into a sliding window.
 * The sliding window can have its length defined in either time or the
 * number of values.
 */
public abstract class AbstractModuleWithWindow<T> extends AbstractSignalPathModule implements ITimeListener {

	enum WindowType {
		EVENTS,
		SECONDS
	}

	IntegerParameter windowLength = new IntegerParameter(this, "windowLength", 0); // needs to be protected to work with auto-init
	StringParameter windowType = new StringParameter(this, "windowType", WindowType.EVENTS.toString().toLowerCase()); // needs to be protected to work with auto-init

	private AbstractWindow window;
	private WindowType selectedWindowType = WindowType.EVENTS;

	@Override
	public void init() {
		addInput(windowLength);
		addInput(windowType);
		super.init();
	}

		/**
	 * Adds an item to the window.
	 * @param item
     */
	protected void addToWindow(T item) {
		if (selectedWindowType == WindowType.EVENTS)
			window.add(item);
		else {
			((TimeWindow) window).add(item, globals.time);
		}
	}

	@Override
	protected void onConfiguration(Map<String, Object> config) {
		selectedWindowType = WindowType.valueOf(windowType.getValue().toUpperCase());
		this.window = createWindow(this.selectedWindowType);
	}

	protected AbstractWindow createWindow(WindowType type) {
		AbstractWindow w;

		if (type == WindowType.EVENTS)
			w = new EventWindow(windowLength.getValue(), createWindowListener());
		else
			w = new TimeWindow(windowLength.getValue(), createWindowListener());

		return w;
	}

	protected abstract WindowListener<T> createWindowListener();

	@Override
	public void setTime(Date time) {
		if (selectedWindowType == WindowType.SECONDS) {
			((TimeWindow)window).setTime(time);
		}
	}

	@Override
	public void sendOutput() {
		window.setLength(windowLength.getValue());
	}

	@Override
	public void clearState() {
		window.clear();
	}

}
