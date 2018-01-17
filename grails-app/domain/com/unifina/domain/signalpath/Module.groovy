package com.unifina.domain.signalpath

class Module implements Comparable {

	Long id
	String name
	String alternativeNames
	String implementingClass
	String jsModule
	String type
	Boolean hide
	ModulePackage modulePackage
	String jsonHelp
	String webcomponent
	
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
		alternativeNames(nullable:true)
		webcomponent(nullable:true)
    }
	
	static mapping = {
		jsonHelp type: 'text'
	}
	
	String toString() {
		return name
	}
	
	/**
	 * Keep this consistent with equals
	 * (return 0 if and only if there is an equals relationship as well)
	 */
	int compareTo(o) {
		int result = this.name.compareTo(o.name)
		if (result==0)
			result = this.id.compareTo(o.id)
		return result
	}
}
