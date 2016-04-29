package com.unifina.api

import grails.validation.Validateable

@Validateable
class SaveDashboardCommand {
	String name

	static constraints = {
		name(blank: false)
	}
}
