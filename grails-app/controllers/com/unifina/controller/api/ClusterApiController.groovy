package com.unifina.controller.api


import com.unifina.security.AllowRole
import com.unifina.security.StreamrApi
import com.unifina.service.ClusterService
import grails.compiler.GrailsCompileStatic
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import org.codehaus.groovy.grails.commons.GrailsApplication

import javax.ws.rs.core.HttpHeaders

@Secured(["IS_AUTHENTICATED_ANONYMOUSLY"])
class ClusterApiController {
	static allowedMethods = [
		canvases: "GET",
		shutdown: "POST",
		repair  : "POST",
	]

	GrailsApplication grailsApplication
	ClusterService clusterService

	@GrailsCompileStatic
	@StreamrApi(allowRoles = AllowRole.ADMIN)
	def canvases() {
		String token = request.getHeader(HttpHeaders.AUTHORIZATION)
		ClusterService.Canvases canvases = clusterService.getCanvases(token, getStreamrNodes())
		render([
			dead: canvases.dead,
			ghost: canvases.ghost,
		] as JSON)
	}

	@GrailsCompileStatic
	@StreamrApi(allowRoles = AllowRole.ADMIN)
	def shutdown() {
		String token = request.getHeader(HttpHeaders.AUTHORIZATION)
		ClusterService.Nodes result = clusterService.shutdown(token, getStreamrNodes())
		render([
			nodeResults: result.nodes,
		] as JSON)
	}

	@GrailsCompileStatic
	@StreamrApi(allowRoles = AllowRole.ADMIN)
	def repair() {
		String token = request.getHeader(HttpHeaders.AUTHORIZATION)
		List<Map<String, Object>> nodes = clusterService.repair(token, getStreamrNodes())
		render([
		    restartedNodes: nodes,
		] as JSON)
	}

	private List<String> getStreamrNodes() {
		(List<String>) grailsApplication.config.streamr.nodes
	}
}
