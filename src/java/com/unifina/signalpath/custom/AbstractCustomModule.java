package com.unifina.signalpath.custom;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.text.SimpleDateFormat;
import java.util.*;

import com.unifina.signalpath.Input;
import com.unifina.signalpath.ModuleWithUI;
import com.unifina.signalpath.Output;
import com.unifina.signalpath.SignalPath;
import com.unifina.utils.Globals;

public abstract class AbstractCustomModule extends ModuleWithUI {

	protected transient SimpleDateFormat df = null;

	protected void debug(Object s) {
		if (getGlobals().getUiChannel()!=null) {
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
					getGlobals().getUiChannel().push(msg, uiChannelId);
					return null;
				}
			});
		}
	}

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

	public void copyStateFromWrapper(SignalPath parentSignalPath,
									 ArrayList<Input> inputs,
									 Map inputsByName,
									 ArrayList<Output> outputs,
									 Map outputsByName,
									 HashSet<Input> drivingInputs,
									 Set<Input> readyInputs,
									 Globals globals) {
		this.parentSignalPath = parentSignalPath;
		this.inputs = new ArrayList<>(inputs);
		this.inputsByName = new HashMap<>(inputsByName);
		this.outputs = new ArrayList<>(outputs);
		this.outputsByName = new HashMap<>(outputsByName);
		this.drivingInputs = new HashSet<>(drivingInputs);
		this.readyInputs = readyInputs;
		this.setGlobals(globals);
	}
}
