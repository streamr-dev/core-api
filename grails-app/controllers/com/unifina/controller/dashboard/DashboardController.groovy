package com.unifina.controller.dashboard

import com.unifina.api.NotPermittedException
import com.unifina.domain.security.Permission.Operation
import com.unifina.domain.security.SecUser
import grails.converters.JSON
import grails.gorm.DetachedCriteria
import grails.plugin.springsecurity.annotation.Secured

import com.unifina.domain.dashboard.Dashboard
import com.unifina.domain.dashboard.DashboardItem

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

	def save() {
		Dashboard dashboard = new Dashboard()
		dashboard.name = params.name
		dashboard.user = springSecurityService.currentUser
		dashboard.save(flush: true, failOnError: true)
		redirect(action: "show", id: dashboard.id)
	}


	def show() {
		return [
			config: grailsApplication.config,
			id: params.id,
			user: springSecurityService.currentUser,
			key: springSecurityService.currentUser?.keys.iterator().next()
		]
	}
}