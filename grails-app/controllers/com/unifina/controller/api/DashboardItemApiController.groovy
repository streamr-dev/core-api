package com.unifina.controller.api

import com.unifina.api.SaveDashboardItemCommand
import com.unifina.domain.dashboard.DashboardItem
import com.unifina.domain.security.SecUser
import com.unifina.security.StreamrApi
import com.unifina.service.DashboardService
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured

@Secured(["IS_AUTHENTICATED_ANONYMOUSLY"])
class DashboardItemApiController {
	DashboardService dashboardService

	@StreamrApi
	def index(String dashboardId) {
		def dashboard = dashboardService.findById(dashboardId, (SecUser) request.apiUser)
		Iterable<DashboardItem> items = dashboard.items
		render(items*.toMap() as JSON)
	}

	@StreamrApi
	def show(String dashboardId, String id) {
		def item = dashboardService.findDashboardItem(dashboardId, id, (SecUser) request.apiUser)
		render(item.toMap() as JSON)
	}

	@StreamrApi
	def save(String dashboardId, SaveDashboardItemCommand command) {
		def item = dashboardService.addDashboardItem(dashboardId, command, (SecUser) request.apiUser)
		render(item.toMap() as JSON)
	}

	@StreamrApi
	def update(String dashboardId, String id, SaveDashboardItemCommand command) {
		def item = dashboardService.updateDashboardItem(dashboardId, id, command, (SecUser) request.apiUser)
		render(item.toMap() as JSON)
	}

	@StreamrApi
	def delete(String dashboardId, String id) {
		dashboardService.deleteDashboardItem(dashboardId, id, (SecUser) request.apiUser)
		render(status: 204)
	}
}
