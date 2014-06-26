package com.unifina.domain.signalpath

class Module implements Comparable {

	Long id
	String name
	String implementingClass
	String jsModule
	String type
	Boolean hide
	ModulePackage modulePackage
	String jsonHelp
	
	SortedSet params
	static belongsTo = [category: ModuleCategory]
	
    static constraints = {
		name()
		implementingClass()
		jsModule()
		type()
		hide(nullable:true)
		modulePackage(nullable:true)
		jsonHelp(nullable:true)
    }
	
	static mapping = {
		jsonHelp type: 'text'
	}
	
	String toString() {
		return name
	}
	
	int compareTo(o) {
		return this.name.compareTo(o.name)
	}
}
