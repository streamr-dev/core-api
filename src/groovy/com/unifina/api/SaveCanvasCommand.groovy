package com.unifina.api

import grails.validation.Validateable

@Validateable
class SaveCanvasCommand {
	String name
	List<Object> modules
	Map settings = [:]
	boolean adhoc

	static constraints = {
		name(blank: false)
		modules(nullable: false)
		settings(nullable: false)
	}
}
