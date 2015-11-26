package com.unifina.signalpath.custom;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.unifina.signalpath.Input;
import com.unifina.signalpath.ModuleWithUI;
import com.unifina.signalpath.Output;
import com.unifina.signalpath.SignalPath;
import com.unifina.utils.Globals;

public abstract class AbstractCustomModule extends ModuleWithUI {

	protected transient SimpleDateFormat df = null;

	protected void debug(String s) {
		if (globals.getUiChannel()!=null) {
			if (df == null) {
				df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
			}
			String t = null;
			if (globals.time != null)
				t = df.format(globals.getTzConverter().getFakeLocalTime(globals.time));

			final HashMap<String, String> msg = new HashMap<>();
			msg.put("type", "debug");
			msg.put("msg", s);
			msg.put("t", t);

			AccessController.doPrivileged(new PrivilegedAction<Void>() {
				@Override
				public Void run() {
					globals.getUiChannel().push(msg, uiChannelId);
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
	}

	public void afterDeserialization(SignalPath parentSignalPath,
									 ArrayList<Input> inputs,
									 Map inputsByName,
									 ArrayList<Output> outputs,
									 Map outputsByName,
									 Globals globals) {
		this.parentSignalPath = parentSignalPath;
		this.inputs = inputs;
		this.inputsByName = inputsByName;
		this.outputs = outputs;
		this.outputsByName = outputsByName;
		this.globals = globals;
	}
}
