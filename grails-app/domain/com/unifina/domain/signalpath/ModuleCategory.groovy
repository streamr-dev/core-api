package com.unifina.domain.signalpath

import grails.persistence.Entity

@Entity
class ModuleCategory implements Comparable {
	String name
	int sortOrder
	ModulePackage modulePackage

	SortedSet modules
	SortedSet subcategories

	Boolean hide

	static hasMany = [modules:Module, subcategories:ModuleCategory]

	static belongsTo = [parent:ModuleCategory]

	static constraints = {
		parent(nullable:true)
		modulePackage(nullable:true)
		hide(nullable:true)
	}

	int compareTo(o) {
		return this.sortOrder.compareTo(o.sortOrder)
	}

}
