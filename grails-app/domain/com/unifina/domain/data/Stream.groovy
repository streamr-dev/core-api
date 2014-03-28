package com.unifina.domain.data

class Stream implements Comparable {
	Long id
	String name
	Feed feed
	String streamConfig
	// An id local to the Feed
	String localId
	
	static constraints = {
		streamConfig(nullable:true)
	}
	
	static mapping = {
		name index:"name_idx"
		localId index: 'localId_idx'
		feed lazy:false
	}
	
	@Override
	public String toString() {
		return name
	}
	
	@Override
	public int compareTo(Object arg0) {
		if (!(arg0 instanceof Stream)) return 0
		else return arg0.name.compareTo(this.name)
	}
}
