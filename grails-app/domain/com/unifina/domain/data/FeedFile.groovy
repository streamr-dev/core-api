package com.unifina.domain.data


class FeedFile {

	Long id
	String name
	@Deprecated 
	Date day
	Date beginDate
	Date endDate
	boolean processed
	Boolean processing
	Boolean processTaskCreated
	Feed feed
	Stream stream
	
	static mapping = {
		version false
	}
	
    static constraints = {
		processing(nullable:true)
		processTaskCreated(nullable:true)
		stream(nullable:true)
    }
	
	String toString() {
		return name
	}
}
