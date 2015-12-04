package com.unifina.domain.signalpath

class UiChannel implements Serializable {
	String id
	String hash
	String name
	
	Module module
	
	static belongsTo = [runningSignalPath: RunningSignalPath]
	
	static mapping = {
		id generator: 'assigned'
	}
	
	static constraints = {
		hash nullable: true
		module nullable:true
		name nullable:true
	}
}
