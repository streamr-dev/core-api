package com.unifina.domain.data

import com.unifina.domain.security.Permission
import com.unifina.domain.signalpath.Module;

class Feed implements Serializable {

	public static final long KAFKA_ID = 7L

	Long id
	String name
	String backtestFeed // TODO: rename?
	String realtimeFeed
	String feedConfig
	
	String timezone
	String preprocessor
	
	String directory

	@Deprecated
	String cacheClass
	String cacheConfig
	@Deprecated
	String parserClass
	@Deprecated
	String messageSourceClass
	String messageSourceConfig
	@Deprecated
	String discoveryUtilClass
	String discoveryUtilConfig

	@Deprecated
	String eventRecipientClass
	@Deprecated
	String keyProviderClass
	@Deprecated
	String streamListenerClass
	@Deprecated
	String streamPageTemplate
	@Deprecated
	String fieldDetectorClass
	@Deprecated
	String dataRangeProviderClass
	
	Module module

	@Deprecated
	Boolean startOnDemand
	@Deprecated
	Boolean bundledFeedFiles

	static hasMany = [permissions: Permission]

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
