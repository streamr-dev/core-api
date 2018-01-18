package com.unifina.api

import grails.validation.Validateable

@Validateable
class SaveDashboardCommand {

	String name
	List<SaveDashboardItemCommand> items
	String layout

	static constraints = {
		name(blank: false)
		layout()
		items(nullable: true)
	}

	// Groovy's .getProperties() sometimes leaves some of the properties out of the map
	def getProperties() {
		return [
				name: name,
				items: items,
				layout: layout
		]
	}
}
