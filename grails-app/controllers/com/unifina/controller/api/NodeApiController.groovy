package com.unifina.controller.api

import com.unifina.api.ApiException
import com.unifina.api.node.NodeRequest
import com.unifina.api.node.NodeRequestDispatcher
import com.unifina.domain.security.SecUser
import com.unifina.domain.signalpath.Canvas
import com.unifina.security.AllowRole
import com.unifina.security.StreamrApi
import com.unifina.service.CanvasService
import com.unifina.service.SerializationService
import com.unifina.service.SignalPathService
import com.unifina.service.TaskService
import com.unifina.signalpath.SignalPath
import com.unifina.signalpath.map.ValueSortedMap
import com.unifina.utils.NetworkInterfaceUtils
import grails.compiler.GrailsCompileStatic
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.codehaus.groovy.grails.web.mapping.LinkGenerator

@Secured(["IS_AUTHENTICATED_ANONYMOUSLY"])
class NodeApiController {

	static allowedMethods = [
		index: "GET",
		canvases: "GET",
		canvasSizes: "GET",
		shutdown: "POST",
		shutdownNode: "POST",
		canvasesNode: "GET"
	]

	GrailsApplication grailsApplication
	CanvasService canvasService
	LinkGenerator grailsLinkGenerator
	SignalPathService signalPathService
	SerializationService serializationService
	TaskService taskService

	NodeRequestDispatcher nodeRequestDispatcher = new NodeRequestDispatcher()


	@GrailsCompileStatic
	@StreamrApi(allowRoles = AllowRole.ADMIN)
	def index() {
		render(getStreamrNodes() as JSON)
	}

	@GrailsCompileStatic
	@StreamrApi(allowRoles = AllowRole.ADMIN)
	def canvases() {
		Collection<Canvas> running = signalPathService.runningSignalPaths*.canvas
		Collection<Canvas> shouldBeRunning = Canvas.findAllByStateAndServer(Canvas.State.RUNNING, ipAddressOfHost())

		Map<String, Canvas> canvasById = (running + shouldBeRunning).collectEntries { Canvas c -> [(c.id): c] }

		Collection<Canvas> areAndShouldBeRunning = (running*.id).intersect(shouldBeRunning*.id)
			.collect { canvasById.get(it) }
		Collection<Canvas> areNotButShouldBeRunning = (shouldBeRunning*.id).minus(running*.id)
			.collect { canvasById.get(it) }
		Collection<Canvas> areButShouldNotBeRunning = (running*.id).minus(shouldBeRunning*.id)
			.collect { canvasById.get(it) }

		render([
			ok: areAndShouldBeRunning*.toMap(),
			shouldBeRunning: areNotButShouldBeRunning*.toMap(),
			shouldNotBeRunning: areButShouldNotBeRunning*.toMap()
		] as JSON)
	}

	@StreamrApi(allowRoles = AllowRole.ADMIN)
	def canvasSizes() {
		def sizePerCanvas = new ValueSortedMap(true)
		signalPathService.runningSignalPaths.each { SignalPath sp ->
			long bytes = serializationService.serialize(sp).length
			sizePerCanvas[sp.canvas.id] = bytes
		}
		render(sizePerCanvas as JSON)
	}

	@GrailsCompileStatic
	@StreamrApi(allowRoles = AllowRole.ADMIN)
	def shutdown() {
		// Shut down task workers
		taskService.stopAllTaskWorkers()

		// Get users of running canvases
		Map<String, SecUser> canvasIdToUser = signalPathService.getUsersOfRunningCanvases()

		// Stop all canvases
		List<Canvas> stoppedCanvases = signalPathService.stopAllLocalCanvases()

		// Discard adhoc canvases
		stoppedCanvases = stoppedCanvases.findAll { !it.adhoc }

		// Create start tasks
		stoppedCanvases.each {
			canvasService.startRemote(it, canvasIdToUser[it.id], false, true)
		}

		render(stoppedCanvases*.toMap() as JSON)
	}

	@GrailsCompileStatic
	@StreamrApi(allowRoles = AllowRole.ADMIN)
	def shutdownNode(String nodeIp) {
		invokeOrRedirect("shutdown", nodeIp, this.&shutdown)
	}

	@GrailsCompileStatic
	@StreamrApi(allowRoles = AllowRole.ADMIN)
	def canvasesNode(String nodeIp) {
		invokeOrRedirect("canvases", nodeIp, this.&canvases)
	}

	private List<String> getStreamrNodes() {
		(List<String>) grailsApplication.config.streamr.nodes
	}

	private String ipAddressOfHost() {
		NetworkInterfaceUtils.getIPAddress(grailsApplication.config.streamr.ip.address.prefixes ?: []).hostAddress
	}

	@GrailsCompileStatic
	private void invokeOrRedirect(String action, String nodeIp, Closure closure) {
		if (NetworkInterfaceUtils.isIpAddressOfCurrentNode(nodeIp)) {
			closure.call()
		} else if (!getStreamrNodes().contains(nodeIp)) {
			throw new ApiException(400, "NOT_A_VALID_NODE", "Not a valid node: '${nodeIp}'")
		} else {
			String path = grailsLinkGenerator.link(controller: "nodeApi", action: action, absolute: false)
			nodeRequestDispatcher.perform(new NodeRequest(nodeIp, path, request, response))
		}
	}
}
