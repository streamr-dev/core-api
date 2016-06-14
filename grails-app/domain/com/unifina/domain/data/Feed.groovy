package com.unifina.domain.data

import com.unifina.domain.signalpath.Module;

class Feed implements Serializable {

	public static final long KAFKA_ID = 7L
	public static final long MONGO_ID = 8L

	Long id
	String name
	String backtestFeed // TODO: rename?
	String realtimeFeed
	String feedConfig
	
	String timezone
	String preprocessor
	
	String directory
	
	String cacheClass
	String cacheConfig
	String parserClass
	String messageSourceClass
	String messageSourceConfig
	String discoveryUtilClass
	String discoveryUtilConfig
	
	String eventRecipientClass
	String keyProviderClass
	String streamListenerClass
	String streamPageTemplate
	String fieldDetectorClass
	String dataRangeProviderClass
	
	Module module
	
	Boolean startOnDemand
	Boolean bundledFeedFiles

	static constraints = {
		backtestFeed(nullable:true)
		realtimeFeed(nullable:true)
		preprocessor(nullable:true)
		directory(nullable:true)
		cacheClass(nullable:true)
		feedConfig(nullable:true)
		cacheConfig(nullable:true)
		messageSourceConfig(nullable:true)
		discoveryUtilClass(nullable:true)
		discoveryUtilConfig(nullable:true)
		startOnDemand(nullable:true)
		bundledFeedFiles(nullable:true)
		fieldDetectorClass(nullable: true)
		dataRangeProviderClass(nullable: true)
	}
	
	@Override
	public int hashCode() {
		return id.hashCode()
	}
	
	@Override
	public boolean equals(Object obj) {
		return obj instanceof Feed && obj.id == this.id
	}

	public Map toMap() {
		return [
		   	id: id,
		   	name: name,
			module: module?.id
		]
	}
}
