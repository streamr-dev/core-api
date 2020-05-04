package com.unifina.controller.api

import com.unifina.security.AllowRole
import com.unifina.security.StreamrApi
import com.unifina.service.ClusterService
import grails.compiler.GrailsCompileStatic
import grails.converters.JSON

import javax.ws.rs.core.HttpHeaders

class ClusterApiController {
	ClusterService clusterService

	@GrailsCompileStatic
	@StreamrApi(allowRoles = AllowRole.ADMIN)
	def canvases() {
		String token = request.getHeader(HttpHeaders.AUTHORIZATION)
		ClusterService.Canvases canvases = clusterService.getCanvases(token)
		render([
			dead: canvases.dead,
			ghost: canvases.ghost,
		] as JSON)
	}

	@GrailsCompileStatic
	@StreamrApi(allowRoles = AllowRole.ADMIN)
	def shutdown() {
		String token = request.getHeader(HttpHeaders.AUTHORIZATION)
		ClusterService.Nodes result = clusterService.shutdown(token)
		render([
			nodeResults: result.nodes,
		] as JSON)
	}

	@GrailsCompileStatic
	@StreamrApi(allowRoles = AllowRole.ADMIN)
	def repair() {
		String token = request.getHeader(HttpHeaders.AUTHORIZATION)
		List<Map<String, Object>> nodes = clusterService.repair(token)
		render([
		    restartedNodes: nodes,
		] as JSON)
	}
}
