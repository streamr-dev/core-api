package com.unifina.domain.signalpath

class ModuleCategory implements Comparable {
	String name
	int sortOrder

	SortedSet modules
	SortedSet subcategories

	Boolean hide

	static hasMany = [modules:Module, subcategories:ModuleCategory]

	static belongsTo = [parent:ModuleCategory]

	static constraints = {
		parent(nullable:true)
		hide(nullable:true)
	}

	int compareTo(o) {
		return this.sortOrder.compareTo(o.sortOrder)
	}

}
