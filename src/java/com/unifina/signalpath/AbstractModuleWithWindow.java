package com.unifina.signalpath;

import com.unifina.datasource.ITimeListener;
import com.unifina.utils.window.AbstractWindow;
import com.unifina.utils.window.EventWindow;
import com.unifina.utils.window.TimeWindow;
import com.unifina.utils.window.WindowListener;

import java.util.Date;
import java.util.Map;

/**
 * Created by henripihkala on 03/03/16.
 */
public abstract class AbstractModuleWithWindow<T> extends AbstractSignalPathModule implements ITimeListener {

	enum WindowType {
		EVENT,
		TIME
	}

	IntegerParameter windowLength = new IntegerParameter(this, "windowLength", 0); // needs to be protected to work with auto-init

	private AbstractWindow window;
	private WindowType windowType = WindowType.EVENT;

	/**
	 * Adds an item to the window.
	 * @param item
     */
	protected void addToWindow(T item) {
		if (windowType == WindowType.EVENT)
			window.add(item);
		else {
			((TimeWindow) window).add(item, globals.time);
		}
	}

	@Override
	protected void onConfiguration(Map<String, Object> config) {
		ModuleOptions options = ModuleOptions.get(config);
		ModuleOption windowType = options.getOption("windowType");
		if (windowType!=null)
			this.windowType = WindowType.valueOf(windowType.getString());

		this.window = createWindow(this.windowType);
	}

	@Override
	public Map<String, Object> getConfiguration() {
		Map<String, Object> config = super.getConfiguration();
		addOption(config, "windowType", ModuleOption.OPTION_STRING, windowType.toString());
		return config;
	}

	protected AbstractWindow createWindow(WindowType type) {
		AbstractWindow w;

		if (type == WindowType.EVENT)
			w = new EventWindow(windowLength.getValue(), createWindowListener());
		else
			w = new TimeWindow(windowLength.getValue(), createWindowListener());

		return w;
	}

	protected abstract WindowListener<T> createWindowListener();

	@Override
	public void setTime(Date time) {
		if (windowType == WindowType.TIME) {
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
