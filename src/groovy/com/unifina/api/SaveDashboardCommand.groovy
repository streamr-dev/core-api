package com.unifina.api

import grails.validation.Validateable

@Validateable
class SaveDashboardCommand {

	String name
	List<SaveDashboardItemCommand> items
	String layout

	static constraints = {
		name(blank: false)
	}
}
