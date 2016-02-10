package com.unifina.domain.signalpath

class UiChannel implements Serializable {
	String id
	String hash
	String name
	
	Module module
	
	static belongsTo = [canvas: Canvas]
	
	static mapping = {
		id generator: 'assigned'
	}
	
	static constraints = {
		hash(nullable: true)
		module(nullable: true)
		name(nullable: true)
	}

	Map<String, Object> toMap() {
		def map = [id: id, name: name]
		if (module == null) {
			return map
		} else {
			return map + [moduleId: module.id, webcomponent: module.webcomponent]
		}
	}
}
