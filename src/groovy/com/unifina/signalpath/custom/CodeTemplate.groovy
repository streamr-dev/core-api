package com.unifina.signalpath.custom

import java.text.SimpleDateFormat


import com.unifina.signalpath.*
import com.unifina.utils.Globals
import com.unifina.utils.TimezoneConverter

@Deprecated
abstract class CodeTemplate extends ModuleWithUI {
	
	Globals globals
	TimezoneConverter tcConverter
	
	private static final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
	
	abstract void sendOutput()
	
	void debug(String s) {
		if (globals.getUiChannel()!=null) {
			String t = null
			if (globals.time!=null)
				t = df.format(tcConverter.getFakeLocalTime(globals.time))

			globals.getUiChannel().push([type:"debug", msg:s, t:t], uiChannelId)
		}
	}
	
	void println(String s) {
		debug(s)
	}
}
