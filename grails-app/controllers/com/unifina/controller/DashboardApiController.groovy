package com.unifina.controller

import com.unifina.api.ValidationException
import com.unifina.domain.Dashboard
import com.unifina.domain.User
import com.unifina.service.*
import grails.converters.JSON

class DashboardApiController {

	DashboardService dashboardService
	SignalPathService signalPathService
	ApiService apiService

	@StreamrApi
	def index(DashboardListParams listParams) {
		if (params.public != null) {
			listParams.publicAccess = params.boolean("public")
		}
		def results = apiService.list(Dashboard, listParams, (User) request.apiUser)
		apiService.addLinkHintToHeader(listParams, results.size(), params, response)
		render(results*.toMap() as JSON)
	}

	@StreamrApi
	def show(String id) {
		def dashboard = dashboardService.findById(id, (User) request.apiUser)
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
	def update(String id, SaveDashboardCommand command) {
		if (!command.validate()) {
			throw new ValidationException(command.errors)
		}
		def dashboard = dashboardService.update(id, command, request.apiUser)
		render(dashboard.toMap() as JSON)
	}

	@StreamrApi
	def delete(String id) {
		dashboardService.deleteById(id, (User) request.apiUser)
		render(status: 204)
	}

	/**
	 * Handles a runtime requests from dashboard view
	 */
	@StreamrApi(authenticationLevel = AuthLevel.NONE)
	def runtimeRequest(String path, Boolean local) {
		def msg = request.JSON
		Map response = signalPathService.runtimeRequest(dashboardService.buildRuntimeRequest(msg, "dashboards/$path", request.apiUser, TokenAuthenticator.getAuthorizationHeader(request)), local ?: false)
		log.info("request: responding with $response")
		render response as JSON
	}

}
