package com.unifina.domain.signalpath

class UiChannel {
	String id
	String hash
	
	Module module
	
	static belongsTo = [runningSignalPath: RunningSignalPath]
	
	static mapping = {
		id generator: 'assigned'
	}
	
	static constraints = {
		hash nullable: true
		module nullable:true
	}
}
