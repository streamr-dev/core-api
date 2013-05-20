package com.unifina.data

import com.unifina.signalpath.Module

class Feed {

	Long id
	String backtestFeed // TODO: rename?
	String realtimeFeed // com.unifina.feed.MessageHub if not extended
	String timezone
	String preprocessor
	String directory
	String cacheClass
	String cacheConfig
	String parserClass
	String messageSourceClass
	String messageSourceConfig

	Module module

	static constraints = {
	}
}
