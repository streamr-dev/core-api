package com.unifina.api

import grails.validation.Validateable

@Validateable
class SaveCanvasCommand {
	String name
	List<Object> modules
	Map settings = [:]

	static constraints = {
		name(blank: false)
		modules(nullable: false)
		settings(nullable: false)
	}
}
