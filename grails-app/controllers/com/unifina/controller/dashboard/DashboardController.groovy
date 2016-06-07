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

	private def getAuthorizedDashboard(long id, Operation op = Operation.READ, boolean prefetchItems = false, Closure action) {
		SecUser user = springSecurityService.currentUser
		Dashboard dashboard = prefetchItems ? Dashboard.findById(params.id, [fetch: [items: "join"]]) : Dashboard.get(id);
		if (!dashboard) {
			response.sendError(404)
			// TODO: alternative (from delete())
			//flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'dashboard.label', default: 'Dashboard'), params.id])}"
			//redirect(action: "list")
		} else if (!permissionService.check(user, dashboard, op)) {
			if (request.xhr) {
				response.status = 403
				render(new NotPermittedException(user.name, "Dashboard", id.toString()).asApiError().toMap() as JSON)
			} else {
				redirect controller: 'login', action: 'denied'
			}
		} else {
			action.call(dashboard, user)
		}
	}

	def list() {
		def user = springSecurityService.currentUser
		def dashboards = permissionService.get(Dashboard, user) { order "lastUpdated", "desc" }
		def shareable = permissionService.get(Dashboard, user, Operation.SHARE).toSet()
		def writable = permissionService.get(Dashboard, user, Operation.WRITE).toSet()
		return [dashboards:dashboards, shareable:shareable, writable:writable, user:user]
	}

	def create() {}

	def save() {
		Dashboard dashboard = new Dashboard()
		dashboard.name = params.name
		dashboard.user = springSecurityService.currentUser
		dashboard.save(flush: true, failOnError: true)
		redirect(action: "show", id: dashboard.id)
	}


	def show() {
		getAuthorizedDashboard(params.long("id")) { Dashboard dashboard, SecUser user ->
			return [
				serverUrl: grailsApplication.config.streamr.ui.server,
				dashboard: dashboard,
				shareable: permissionService.canShare(user, dashboard)
			]
		}
	}
}