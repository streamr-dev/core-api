package com.unifina.signalpath

class Module implements Comparable {

	Long id
	String name
	String implementingClass
	String jsModule
	String type
	Boolean hide
	ModulePackage modulePackage
	
	SortedSet params
	static belongsTo = [category: ModuleCategory]
	
    static constraints = {
		name()
		implementingClass()
		jsModule()
		type()
		hide(nullable:true)
		modulePackage(nullable:true)
    }
	
	String toString() {
		return name
	}
	
	int compareTo(o) {
		return this.name.compareTo(o.name)
	}
}
