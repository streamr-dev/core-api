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
		parentSignalPath = null;
		inputs = null;
		inputsByName = null;
		outputs = null;
		outputsByName = null;
		drivingInputs = null;
		readyInputs = null;
	}

	void copyStateFromWrapper(StoredCustomModuleState customModuleState) {
		this.parentSignalPath = customModuleState.getParentSignalPath();
		this.inputs = customModuleState.getInputs();
		this.inputsByName = customModuleState.getInputsByName();
		this.outputs = customModuleState.getOutputs();
		this.outputsByName = customModuleState.getOutputsByName();
		this.drivingInputs = customModuleState.getDrivingInputs();
		this.readyInputs = customModuleState.getReadyInputs();
	}

	void setParentWrapper(AbstractJavaCodeWrapper parentWrapper) {
		this.parentWrapper = parentWrapper;
	}

	StoredCustomModuleState getStoredState() {
		return new StoredCustomModuleState(parentSignalPath, inputs, inputsByName, outputs, outputsByName, drivingInputs, readyInputs);
	}
}
