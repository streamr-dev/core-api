package com.unifina.controller.api

import com.streamr.api.client.CanvasesPerNode
import com.streamr.api.client.StreamrClient
import com.unifina.domain.signalpath.Canvas
import com.unifina.security.AllowRole
import com.unifina.security.StreamrApi
import grails.compiler.GrailsCompileStatic
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import org.codehaus.groovy.grails.commons.GrailsApplication

import javax.ws.rs.core.HttpHeaders

@Secured(["IS_AUTHENTICATED_ANONYMOUSLY"])
class ClusterApiController {
	static allowedMethods = [
		index: "GET",
	]

	GrailsApplication grailsApplication
	StreamrClient client

	@GrailsCompileStatic
	@StreamrApi(allowRoles = AllowRole.ADMIN)
	def index() {
		List<Canvas> dead = new ArrayList<Canvas>()
		List<Canvas> ghost = new ArrayList<Canvas>()
		String token = request.getHeader(HttpHeaders.AUTHORIZATION)

		for (String ip : getStreamrNodes()) {
			CanvasesPerNode canvases = client.canvasesPerNode(token, ip)
			if (canvases.shouldBeRunning != null) {
				dead.addAll(canvases.shouldBeRunning)
			}
			if (canvases.shouldNotBeRunning != null) {
				ghost.addAll(canvases.shouldNotBeRunning)
			}
		}
		render([
			dead: dead,
			ghost: ghost
		] as JSON)
	}

	private List<String> getStreamrNodes() {
		(List<String>) grailsApplication.config.streamr.nodes
	}
}
