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
	String format
	
	static mapping = {
		version false
	}
	
    static constraints = {
		processing(nullable:true)
		processTaskCreated(nullable:true)
		stream(nullable:true)
		format(nullable:true)
    }
	
	String toString() {
		return name
	}
}
