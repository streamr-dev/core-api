package com.unifina.service

import com.streamr.api.client.CanvasesPerNode
import com.streamr.api.client.StreamrClient
import com.unifina.domain.security.SecUser
import com.unifina.domain.signalpath.Canvas
import groovy.transform.CompileStatic

class ClusterService {
	CanvasService canvasService
	StreamrClient streamrClient

	@CompileStatic
	Canvases getCanvases(String token, List<String> streamrNodes) {
		List<Map<String, Object>> dead = new ArrayList<Map<String, Object>>()
		List<Map<String, Object>> ghost = new ArrayList<Map<String, Object>>()
		for (String ip : streamrNodes) {
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
	Nodes shutdown(String token, List<String> streamrNodes) {
		List<Map<String, Object>> nodeResults = new ArrayList<Map<String, Object>>()
		for (String ip : streamrNodes) {
			List<Map<String, Object>> result = streamrClient.shutdown(token, ip)
			nodeResults.addAll(result)
		}
		return new Nodes(nodes: nodeResults)
	}

	@CompileStatic
    List<Map<String, Object>> repair(String token, List<String> streamrNodes) {
		Canvases canvases = getCanvases(token, streamrNodes)
		for (Map<String, Object> canvas : canvases.dead) {
			String id = canvas.get("id")
			String startedById = canvas.get("startedById")

			Canvas c = Canvas.get(id)
			SecUser u = SecUser.get(startedById)
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
}
