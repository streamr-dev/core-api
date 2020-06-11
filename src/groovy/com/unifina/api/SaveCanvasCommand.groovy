package com.unifina.api

import grails.validation.Validateable
import groovy.transform.ToString

@Validateable
@ToString
class SaveCanvasCommand {
	String name
	List<Object> modules
	Map settings = [:]
	boolean adhoc = false

	static constraints = {
		name(blank: false)
		modules(nullable: false)
		settings(nullable: false)
	}
}
