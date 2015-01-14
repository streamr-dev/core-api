package com.unifina.controller.dashboard

import grails.plugin.springsecurity.annotation.Secured

@Secured(["ROLE_ADMIN"])
class DashboardController {
	def grailsApplication
	
	def index() {
		return [serverUrl: grailsApplication.config.unifina.ui.server]
	}
}
