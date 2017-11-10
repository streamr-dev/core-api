package com.unifina.signalpath.custom;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.text.SimpleDateFormat;
import java.util.*;

import com.unifina.datasource.ITimeListener;
import com.unifina.signalpath.*;
import com.unifina.utils.Globals;

public abstract class AbstractCustomModule extends ModuleWithUI implements ITimeListener {

	protected transient SimpleDateFormat df = null;
	private transient AbstractJavaCodeWrapper parentWrapper;

	protected void debug(Object s) {
		if (df == null) {
			df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		}
		String t = null;
		if (getGlobals().time != null)
			t = df.format(getGlobals().getTzConverter().getFakeLocalTime(getGlobals().time));

		final HashMap<String, String> msg = new HashMap<>();
		msg.put("type", "debug");
		msg.put("msg", s != null ? s.toString() : null);
		msg.put("t", t);

		AccessController.doPrivileged(new PrivilegedAction<Void>() {
			@Override
			public Void run() {
				parentWrapper.pushToUiChannel(msg);
				return null;
			}
		});
	}

	@Override
	public void setTime(Date time) {}

	@Override
	public void beforeSerialization() {
		super.beforeSerialization();
		setParentSignalPath(null);
		setInputs(null);
		setInputsByName(null);
		setOutputs(null);
		setOutputsByName(null);
		setDrivingInputs(null);
		setReadyInputs(null);
	}

	void copyStateFromWrapper(StoredCustomModuleState customModuleState) {
		setParentSignalPath(customModuleState.getParentSignalPath());
		setInputs(customModuleState.getInputs());
		setInputsByName(customModuleState.getInputsByName());
		setOutputs(customModuleState.getOutputs());
		setOutputsByName(customModuleState.getOutputsByName());
		setDrivingInputs(customModuleState.getDrivingInputs());
		setReadyInputs(customModuleState.getReadyInputs());
	}

	void setParentWrapper(AbstractJavaCodeWrapper parentWrapper) {
		this.parentWrapper = parentWrapper;
	}

	StoredCustomModuleState getStoredState() {
		return new StoredCustomModuleState(getParentSignalPath(),
			new ArrayList<>(Arrays.asList(getInputs())),
			getInputsByName(),
			new ArrayList<>(Arrays.asList(getOutputs())),
			getOutputsByName(),
			getDrivingInputs(),
			getReadyInputs()
		);
	}
}
