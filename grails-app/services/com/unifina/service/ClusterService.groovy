package com.unifina.service

import com.streamr.api.client.CanvasesPerNode
import com.streamr.api.client.StreamrClient
import com.unifina.domain.security.User
import com.unifina.domain.signalpath.Canvas
import groovy.transform.CompileStatic
import org.codehaus.groovy.grails.commons.GrailsApplication

class ClusterService {
	CanvasService canvasService
	StreamrClient streamrClient
	GrailsApplication grailsApplication

	@CompileStatic
	Canvases getCanvases(String token) {
		List<Map<String, Object>> dead = new ArrayList<Map<String, Object>>()
		List<Map<String, Object>> ghost = new ArrayList<Map<String, Object>>()
		for (String ip : getStreamrNodes()) {
			CanvasesPerNode canvases = streamrClient.canvasesPerNode(token, ip)
			if (canvases.shouldBeRunning != null) {
				dead.addAll(canvases.shouldBeRunning)
			}
			if (canvases.shouldNotBeRunning != null) {
				ghost.addAll(canvases.shouldNotBeRunning)
			}
		}
		return new Canvases(dead: dead, ghost: ghost)
	}

	@CompileStatic
	Nodes shutdown(String token) {
		List<Map<String, Object>> nodeResults = new ArrayList<Map<String, Object>>()
		for (String ip : getStreamrNodes()) {
			List<Map<String, Object>> result = streamrClient.shutdown(token, ip)
			nodeResults.addAll(result)
		}
		return new Nodes(nodes: nodeResults)
	}

	@CompileStatic
    List<Map<String, Object>> repair(String token) {
		Canvases canvases = getCanvases(token)
		for (Map<String, Object> canvas : canvases.dead) {
			String id = canvas.get("id")
			String startedById = canvas.get("startedById")

			Canvas c = Canvas.get(id)
			c.state = Canvas.State.STOPPED
			c.save()
			User u = User.get(startedById)
			boolean forceReset = true
			boolean resetOnError = true
			canvasService.startRemote(c, u, forceReset, resetOnError)
		}
		return canvases.dead
    }

	static class Canvases {
		List<Map<String, Object>> dead = new ArrayList<Map<String, Object>>()
		List<Map<String, Object>> ghost = new ArrayList<Map<String, Object>>()
	}

	static class Nodes {
		List<Map<String, Object>> nodes = new ArrayList<Map<String, Object>>()
	}

	private List<String> getStreamrNodes() {
		(List<String>) grailsApplication.config.streamr.engine.nodes
	}
}
