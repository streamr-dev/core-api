package com.unifina.data

import com.unifina.signalpath.Module

class Feed {

	Long id
	String backtestFeed
	String realtimeFeed
	String timezone
	String preprocessor
	String directory
	
	Module module
	
    static constraints = {
    }
}
