package com.unifina.signalpath;

import com.unifina.datasource.ITimeListener;
import com.unifina.utils.window.AbstractWindow;
import com.unifina.utils.window.EventWindow;
import com.unifina.utils.window.TimeWindow;
import com.unifina.utils.window.WindowListener;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Abstraction of a module that collects values into a sliding window.
 * The sliding window can have its length defined in either time or the
 * number of values.
 */
public abstract class AbstractModuleWithWindow<T> extends AbstractSignalPathModule implements ITimeListener {

	enum WindowType {
		EVENTS,
		SECONDS,
		MINUTES,
		HOURS,
		DAYS
	}

	IntegerParameter windowLength = new IntegerParameter(this, "windowLength", 0); // needs to be protected to work with auto-init
	WindowTypeParameter windowType = new WindowTypeParameter(this, "windowType", WindowType.EVENTS.toString().toLowerCase()); // needs to be protected to work with auto-init

	private AbstractWindow[] windows;
	private WindowType selectedWindowType = WindowType.EVENTS;

	private int dimensions = 1;
	private Integer cachedLength = null;

	@Override
	public void init() {
		addInput(windowLength);
		addInput(windowType);
		super.init();
	}

	/**
	 * Adds an item to the window (in multidimensional case, use addToWindow(item, dimension)).
	 * @param item
     */
	protected void addToWindow(T item) {
		addToWindow(item, 0);
	}

	protected void addToWindow(T item, int dimension) {
		if (selectedWindowType == WindowType.EVENTS)
			windows[dimension].add(item);
		else {
			((TimeWindow) windows[dimension]).add(item, globals.time);
		}
	}

	@Override
	protected void onConfiguration(Map<String, Object> config) {
		selectedWindowType = WindowType.valueOf(windowType.getValue().toUpperCase());
		this.windows = new AbstractWindow[dimensions];
		for (int d = 0; d< dimensions; d++) {
			windows[d] = createWindow(this.selectedWindowType, d);
		}
	}

	protected AbstractWindow createWindow(WindowType type, int dimension) {
		AbstractWindow w;

		if (type == WindowType.EVENTS) {
			w = new EventWindow(windowLength.getValue(), createWindowListener(dimension));
		}
		else {
			// Expects the enum names between TimeUnit and WindowType to be equal
			TimeUnit timeUnit = TimeUnit.valueOf(windowType.getValue());
			w = new TimeWindow(windowLength.getValue(), timeUnit, createWindowListener(dimension));
		}

		return w;
	}

	/**
	 * Should add input values to the windows by calling addToWindow()
	 */
	protected abstract void handleInputValues();

	/**
	 * Should send out the current value
	 */
	protected abstract void doSendOutput();

	/**
	 * Should create a WindowListener for the given dimension index
	 * @param dimension
	 * @return
     */
	protected abstract WindowListener<T> createWindowListener(int dimension);

	@Override
	public void setTime(Date time) {
		if (selectedWindowType != WindowType.EVENTS) {
			boolean windowChanged = false;
			for (int d = 0; d< dimensions; d++) {
				int initialSize = windows[d].getSize();
				((TimeWindow) windows[d]).setTime(time);
				if (windows[d].getSize() != initialSize)
					windowChanged = true;
			}

			if (windowChanged) {
				doSendOutput();
			}
		}
	}

	@Override
	public void sendOutput() {
		if (cachedLength==null || !windowLength.getValue().equals(cachedLength)) {
			cachedLength = windowLength.getValue();
			for (int d = 0; d< dimensions; d++) {
				windows[d].setLength(cachedLength);
			}
		}

		handleInputValues();
		doSendOutput();
	}

	@Override
	public void clearState() {
		for (int d = 0; d< dimensions; d++) {
			windows[d].clear();
		}
	}

	class WindowTypeParameter extends StringParameter {

		public WindowTypeParameter(AbstractSignalPathModule owner, String name, String defaultValue) {
			super(owner, name, defaultValue);
		}

		@Override
		public Map<String, Object> getConfiguration() {
			Map<String, Object> config = super.getConfiguration();

			List<Map<String, String>> possibleValues = new ArrayList<>();
			for (WindowType wt : WindowType.values()) {
				Map<String,String> m = new LinkedHashMap<>();
				m.put("name", wt.name().toLowerCase());
				m.put("value", wt.name());
				possibleValues.add(m);
			}
			config.put("possibleValues", possibleValues);

			return config;
		}
	}

	public int getDimensions() {
		return dimensions;
	}

	public void setDimensions(int dimensions) {
		if (windows==null) {
			this.dimensions = dimensions;
		}
		else {
			throw new RuntimeException("Module has already been configured, must set dimension earlier!");
		}
	}

}
