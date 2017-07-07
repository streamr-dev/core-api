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
		layout()
	}

	// Groovy's .getProperties() sometimes leaves some of the properties out of the map
	def getProperties() {
		return [
				id: id,
				name: name,
				items: items,
				layout: layout
		]
	}
}
