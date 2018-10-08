package com.unifina.service

import com.streamr.api.client.CanvasesPerNode
import com.streamr.api.client.StreamrClient
import com.unifina.domain.signalpath.Canvas
import groovy.transform.CompileStatic

class ClusterService {
	CanvasService canvasService
	StreamrClient streamrClient

	@CompileStatic
	Canvases getCanvases(String token, List<String> streamrNodes) {
		List<Canvas> dead = new ArrayList<Canvas>()
		List<Canvas> ghost = new ArrayList<Canvas>()
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
    List<Canvas> repair(String token, List<String> streamrNodes) {
		Canvases canvases = getCanvases(token, streamrNodes)
		for (Canvas c : canvases.dead) {
			boolean forceReset = false
			boolean resetOnError = true
			canvasService.startRemote(c, c.startedBy, forceReset, resetOnError)
		}
		return canvases.dead
    }

	static class Canvases {
		List<Canvas> dead = new ArrayList<Canvas>()
		List<Canvas> ghost = new ArrayList<Canvas>()
	}

	static class Nodes {
		List<Map<String, Object>> nodes = new ArrayList<Map<String, Object>>()
	}
}
