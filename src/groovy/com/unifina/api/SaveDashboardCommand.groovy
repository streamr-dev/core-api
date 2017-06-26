package com.unifina.api

import grails.validation.Validateable

@Validateable
class SaveDashboardCommand {

	String id

	String name
	List<SaveDashboardItemCommand> items
	String layout

	static constraints = {
		id(nullable: false)
		name(blank: false)
		layout(blank: false)
	}
}
