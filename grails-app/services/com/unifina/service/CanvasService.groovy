package com.unifina.service

import com.unifina.api.*
import com.unifina.domain.dashboard.Dashboard
import com.unifina.domain.dashboard.DashboardItem
import com.unifina.domain.data.Stream
import com.unifina.domain.security.Permission
import com.unifina.domain.security.SecUser
import com.unifina.domain.signalpath.Canvas
import com.unifina.exceptions.CanvasUnreachableException
import com.unifina.serialization.SerializationException
import com.unifina.signalpath.UiChannelIterator
import com.unifina.task.CanvasDeleteTask
import com.unifina.task.CanvasStartTask
import com.unifina.utils.Globals
import com.unifina.utils.GlobalsFactory
import grails.converters.JSON
import grails.transaction.Transactional
import groovy.json.JsonBuilder
import groovy.transform.CompileStatic
import org.codehaus.groovy.grails.web.json.JSONObject
import org.codehaus.groovy.grails.web.mapping.LinkGenerator

class CanvasService {

	SignalPathService signalPathService
	TaskService taskService
	PermissionService permissionService
	DashboardService dashboardService
	StreamService streamService
	LinkGenerator linkGenerator

	@CompileStatic
	Map reconstruct(Canvas canvas, SecUser user) {
		Map signalPathMap = (JSONObject) JSON.parse(canvas.json)
		return reconstructFrom(signalPathMap, user)
	}

	@CompileStatic
	Canvas createNew(SaveCanvasCommand command, SecUser user) {
		Canvas canvas = new Canvas()
		updateExisting(canvas, command, user, true)
		permissionService.systemGrantAll(user, canvas)
		return canvas
	}

	@CompileStatic
	void updateExisting(Canvas canvas, SaveCanvasCommand command, SecUser user, boolean resetUi = false) {
		if (!command.validate()) {
			throw new ValidationException(command.errors)
		}
		if (canvas.state != Canvas.State.STOPPED) {
			throw new InvalidStateException("Cannot update canvas with state " + canvas.state)
		}

		Map newSignalPathMap = constructNewSignalPathMap(canvas, command, user, resetUi)

		canvas.name = newSignalPathMap.name
		canvas.hasExports = newSignalPathMap.hasExports
		canvas.json = new JsonBuilder(newSignalPathMap).toPrettyString() // JsonBuilder is more stable than "as JSON"
		canvas.state = Canvas.State.STOPPED
		canvas.adhoc = command.isAdhoc()

		// clear serialization
		canvas.serialization?.delete()
		canvas.serialization = null
		canvas.save(flush: true, failOnError: true)
	}

	/**
	 * Deletes a Canvas along with any resources (DashboardItems, Streams) pointing to it.
	 * It can be deleted after a delay to allow resource consumers to finish up.
     */
	@Transactional
	void deleteCanvas(Canvas canvas, SecUser user, boolean delayed = false) {
		if (delayed) {
			taskService.createTask(CanvasDeleteTask, CanvasDeleteTask.getConfig(canvas), "delete-canvas", user, 30 * 60 * 1000)
		} else if (canvas.state == Canvas.State.RUNNING) {
			throw new ApiException(409, "CANNOT_DELETE_RUNNING", "Cannot delete running canvas.")
		} else {
			Collection<Stream> uiChannels = Stream.findAllByUiChannelCanvas(canvas)
			uiChannels.each {
				streamService.deleteStream(it)
			}
			canvas.delete(flush: true)
		}
	}

	void start(Canvas canvas, boolean clearSerialization, SecUser asUser) {
		if (canvas.state == Canvas.State.RUNNING) {
			throw new InvalidStateException("Cannot run canvas $canvas.id because it's already running. Stop it first.")
		}

		if (clearSerialization) {
			signalPathService.clearState(canvas)
		}

		Map signalPathContext = canvas.toMap().settings

		try {
			signalPathService.startLocal(canvas, signalPathContext, asUser)
		} catch (SerializationException ex) {
			log.error("De-serialization failure caused by (BELOW)", ex.cause)
			String msg = "Could not load (deserialize) previous state of canvas $canvas.id."
			throw new ApiException(500, "LOADING_PREVIOUS_STATE_FAILED", msg)
		}
	}

	void startRemote(Canvas canvas, SecUser user, boolean forceReset=false, boolean resetOnError=true) {
		taskService.createTask(CanvasStartTask, CanvasStartTask.getConfig(canvas, forceReset, resetOnError), "canvas-start", user)
	}

	@Transactional(noRollbackFor=[CanvasUnreachableException])
	void stop(Canvas canvas, SecUser user) throws ApiException {
		if (canvas.state != Canvas.State.RUNNING) {
			throw new InvalidStateException("Canvas $canvas.id not currently running.")
		}

		try {
			signalPathService.stopRemote(canvas, user)
		} catch (CanvasUnreachableException e) {
			canvas.state = Canvas.State.STOPPED
			canvas.save(failOnError: true, flush: true)
			throw e
		}
	}

	/**
	 * Gets a canvas by id, authorizing the given user for the given Operation.
	 * Throws an exception if authorization fails.
     */
	@CompileStatic
	Canvas authorizedGetById(String id, SecUser user, Permission.Operation op) {
		def canvas = Canvas.get(id)
		if (!canvas) {
			throw new NotFoundException("Canvas", id)
		} else if (!hasCanvasPermission(canvas, user, op)) {
			throw new NotPermittedException(user?.username, "Canvas", id, op.id)
		} else {
			return canvas
		}
	}

	/**
	 * Checks if the user has permission to access a module on a canvas.
	 * Throws an exception if authorization fails.
	 *
	 * Permission can be granted via:
	 *
	 * - Permission to the canvas that contains the module
	 * - Permission to a dashboard that contains the module from that canvas
	 *
	 * Deprecated: runtime permission checking now much more comprehensive in SignalPathService
	 */
	@CompileStatic
	Map authorizedGetModuleOnCanvas(String canvasId, Integer moduleId, String dashboardId, SecUser user, Permission.Operation op) {
		Canvas canvas = Canvas.get(canvasId)

		if (!canvas) {
			throw new NotFoundException("Canvas", canvasId)
		} else if (!hasCanvasPermission(canvas, user, op) && !hasModulePermissionViaDashboard(canvas, moduleId, dashboardId, user, op)) {
			throw new NotPermittedException(user?.username, "Canvas", canvasId, op.id)
		} else {
			Map canvasMap = canvas.toMap()
			Map moduleMap = (Map) canvasMap.modules.find { it["hash"].toString() == moduleId?.toString() }

			if (!moduleMap) {
				throw new NotFoundException("Module $moduleId not found on canvas $canvasId", "Module", moduleId?.toString())
			}

			return moduleMap
		}
	}

	@CompileStatic
	void resetUiChannels(Map signalPathMap) {
		for (UiChannelIterator.Element element in UiChannelIterator.over(signalPathMap)) {
			element.uiChannelData.id = null
		}
	}

	@CompileStatic
	String getCanvasURL(Canvas canvas) {
		return linkGenerator.link(controller: 'canvas', action: 'editor', id: canvas.id, absolute: true)
	}

	private boolean hasCanvasPermission(Canvas canvas, SecUser user, Permission.Operation op) {
		return op == Permission.Operation.READ && canvas.example || permissionService.check(user, canvas, op)
	}

	private boolean hasModulePermissionViaDashboard(Canvas canvas, Integer moduleId, String dashboardId, SecUser user, Permission.Operation op) {
		if (!dashboardId) {
			return false
		}

		// Throws if no access
		Dashboard dashboard = dashboardService.authorizedGetById(dashboardId, user, op)
		// Check that the dashboard actually contains the module
		return dashboard?.items?.find { DashboardItem it ->
			it.canvas.id == canvas.id && it.module == moduleId
		} != null
	}

	private Map constructNewSignalPathMap(Canvas canvas, SaveCanvasCommand command, SecUser user, boolean resetUi) {
		Map inputSignalPathMap = JSON.parse(canvas.json != null ? canvas.json : "{}")

		inputSignalPathMap.name = command.name
		inputSignalPathMap.modules = command.modules
		inputSignalPathMap.settings = command.settings

		if (resetUi) {
			resetUiChannels(inputSignalPathMap)
		}

		return reconstructFrom(inputSignalPathMap, user)
	}

	/**
	 * Rebuild JSON to check it is ok and up-to-date
	 */
	private Map reconstructFrom(Map signalPathMap, SecUser user) {
		Globals globals = GlobalsFactory.createInstance(signalPathMap.settings ?: [:], user)
		return signalPathService.reconstruct(signalPathMap, globals)
	}

}
