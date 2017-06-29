package com.unifina.controller.api

import com.unifina.api.SaveDashboardCommand
import com.unifina.api.ValidationException
import com.unifina.domain.dashboard.Dashboard
import com.unifina.domain.security.Permission
import com.unifina.domain.security.SecUser
import com.unifina.security.AuthLevel
import com.unifina.security.StreamrApi
import com.unifina.service.ApiService
import com.unifina.service.DashboardService
import com.unifina.service.PermissionService
import com.unifina.service.SignalPathService
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import org.json.JSONObject

@Secured(["IS_AUTHENTICATED_ANONYMOUSLY"])
class DashboardApiController {

	DashboardService dashboardService
	SignalPathService signalPathService
	PermissionService permissionService
	ApiService apiService

	@StreamrApi
	def index() {
		def criteria = apiService.createListCriteria(params, ["name"], {
			// Filter by exact name
			if (params.name) {
				eq "name", params.name
			}
		})
		def dashboards = permissionService.get(Dashboard, request.apiUser, Permission.Operation.READ, apiService.isPublicFlagOn(params), criteria)
		render(dashboards*.toSummaryMap() as JSON)
	}

	@StreamrApi
	def show(String id) {
		def dashboard = dashboardService.findById(id, (SecUser) request.apiUser)
		render(dashboard.toMap() as JSON)
	}

	@StreamrApi
	def save(SaveDashboardCommand command) {
		if (!command.validate()) {
			throw new ValidationException(command.errors)
		}
		def dashboard = dashboardService.create(command, request.apiUser)
		render(dashboard.toMap() as JSON)
	}

	@StreamrApi
	def update(SaveDashboardCommand command) {
		if (!command.validate()) {
			throw new ValidationException(command.errors)
		}
		def dashboard = dashboardService.update(command, request.apiUser)
		render(dashboard.toMap() as JSON)
	}

	@StreamrApi
	def delete(String id) {
		dashboardService.deleteById(id, (SecUser) request.apiUser)
		render(status: 204)
	}

	/**
	 * Handles a runtime requests from dashboard view
	 */
	@StreamrApi(authenticationLevel = AuthLevel.NONE)
	def runtimeRequest(String path, Boolean local) {
		def msg = request.JSON
		Map response = signalPathService.runtimeRequest(dashboardService.buildRuntimeRequest(msg, "dashboards/$path", request.apiUser), local ? true : false)
		log.info("request: responding with $response")
		render response as JSON
	}

}
