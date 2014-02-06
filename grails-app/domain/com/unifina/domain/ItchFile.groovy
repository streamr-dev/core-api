package com.unifina.domain

import com.unifina.domain.data.Feed;

// Rename to FeedFile or something like that
class ItchFile {

	String name
	
	@Deprecated
	String path
	Date day
	
	@Deprecated
	boolean orderBookDirectoryLoaded
	
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
