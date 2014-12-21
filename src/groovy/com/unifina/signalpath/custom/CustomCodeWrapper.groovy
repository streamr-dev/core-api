package com.unifina.signalpath.custom

import java.text.SimpleDateFormat;

import com.unifina.signalpath.AbstractSignalPathModule
import com.unifina.utils.Globals

class CustomCodeWrapper extends AbstractCodeWrapper {
	
	String getHeader() {return """
public class [[CLASSNAME]] extends ModuleWithUI {

	private final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")

	public [[CLASSNAME]]() {
		super();
	}

	void initialize() {
		super.initialize()
		setup()
	}

	void debug(String s) {
		if (globals.getUiChannel()!=null) {
			String t = null
			if (globals.time!=null)
				t = df.format(globals.tzConverter.getFakeLocalTime(globals.time))

			globals.getUiChannel().push([type:"debug", msg:s, t:t], uiChannelId)
		}
	}

	void println(String s) {
		debug(s)
	}

""" }
	
	String getDefaultCode() {return """// Define inputs and outputs here
// def param = new IntegerParameter(this, "param", 0)
// def input = new TimeSeriesInput(this, "in")
// def output = new TimeSeriesOutput(this, "out")

void setup() {
	// Initialize local variables
}

void sendOutput() {
	//Write your module code here

}

void clearState() {
	// Clear internal state at end-of-day
}
""" }
	
	String getFooter() {return "}" }

}
