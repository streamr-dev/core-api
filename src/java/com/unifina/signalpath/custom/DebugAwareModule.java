package com.unifina.signalpath.custom;

import java.text.SimpleDateFormat;
import java.util.HashMap;

import com.unifina.signalpath.ModuleWithUI;

public abstract class DebugAwareModule extends ModuleWithUI {

	protected final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

	protected void debug(String s) {
		if (globals.getUiChannel()!=null) {
			String t = null;
			if (globals.time!=null)
				t = df.format(globals.getTzConverter().getFakeLocalTime(globals.time));

			HashMap<String,String> msg = new HashMap<>();
			msg.put("type","debug");
			msg.put("msg",s);
			msg.put("t",t);
			globals.getUiChannel().push(msg, uiChannelId);
		}
	}
	
}
