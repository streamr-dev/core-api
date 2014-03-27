package com.unifina.domain.data


class FeedFile {

	String name
	
//	@Deprecated
//	String path
	Date day
	
//	@Deprecated
//	boolean orderBookDirectoryLoaded
	
	boolean processed
	Feed feed
	
	static mapping = {
		version false
	}
	
    static constraints = {
		feed(unique: ['day'])
    }
	
	
	String toString() {
		return name
	}
}
