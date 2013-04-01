package com.unifina.signalpath.custom

import java.text.SimpleDateFormat

import com.unifina.signalpath.*
import com.unifina.utils.Globals
import com.unifina.utils.TimezoneConverter

/**
 * This could be added to custom modules as a mixin. However I could
 * not get it to work, it always crashed the JVM! 2013-03-01
 * @author Henri
 */
@Category(AbstractSignalPathModule) class DebugAbility {

	// Requires the following methods from the class where mixed in: getParentSignalPath(), getHash()
		
//	SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
//	Globals g = 
//	TimezoneConverter tzConverter = g.tzConverter
//	
//	void debug(String s) {
//		if (getParentSignalPath()?.returnChannel) {
//			String t = null
//			if (g.time!=null)
//				t = df.format(tzConverter.getFakeLocalTime(g.time))
//
//			getParentSignalPath().returnChannel.sendPayload(getHash(), [type:"debug", msg:s, t:t])
//		}
//	}
}
