package com.unifina.controller.api

import com.unifina.api.SaveDashboardItemCommand
import com.unifina.domain.DashboardItem
import com.unifina.domain.User
import com.unifina.security.StreamrApi
import com.unifina.service.DashboardService
import grails.converters.JSON

class DashboardItemApiController {
	DashboardService dashboardService

	@StreamrApi
	def index(String dashboardId) {
		def dashboard = dashboardService.findById(dashboardId, (User) request.apiUser)
		Iterable<DashboardItem> items = dashboard.items
		render(items*.toMap() as JSON)
	}

	@StreamrApi
	def show(String dashboardId, String id) {
		def item = dashboardService.findDashboardItem(dashboardId, id, (User) request.apiUser)
		render(item.toMap() as JSON)
	}

	@StreamrApi
	def save(String dashboardId, SaveDashboardItemCommand command) {
		def item = dashboardService.addDashboardItem(dashboardId, command, (User) request.apiUser)
		render(item.toMap() as JSON)
	}

	@StreamrApi
	def update(String dashboardId, String id, SaveDashboardItemCommand command) {
		def item = dashboardService.updateDashboardItem(dashboardId, id, command, (User) request.apiUser)
		render(item.toMap() as JSON)
	}

	@StreamrApi
	def delete(String dashboardId, String id) {
		dashboardService.deleteDashboardItem(dashboardId, id, (User) request.apiUser)
		render(status: 204)
	}
}
