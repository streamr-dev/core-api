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
	
	static mapping = {
		version false
	}
	
    static constraints = {
		feed(unique: ['day'])
		processing(nullable:true)
		processTaskCreated(nullable:true)
    }
	
	String toString() {
		return name
	}
}
