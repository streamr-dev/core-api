package com.unifina.controller.dashboard

import com.unifina.domain.security.Permission.Operation
import grails.plugin.springsecurity.annotation.Secured

import com.unifina.domain.dashboard.Dashboard

@Secured(["ROLE_USER"])
class DashboardController {
	def grailsApplication
	def springSecurityService
	def permissionService

	static defaultAction = "list"

	def list() {
		def user = springSecurityService.currentUser
		def dashboards = permissionService.get(Dashboard, user) { order "lastUpdated", "desc" }
		def shareable = permissionService.get(Dashboard, user, Operation.SHARE).toSet()
		def writable = permissionService.get(Dashboard, user, Operation.WRITE).toSet()
		return [dashboards:dashboards, shareable:shareable, writable:writable, user:user]
	}


	def editor() {
		return [
			config: grailsApplication.config
		]
	}
}