package com.unifina.service

import com.unifina.api.ApiException
import com.unifina.api.InvalidStateException
import com.unifina.api.NotFoundException
import com.unifina.api.NotPermittedException
import com.unifina.api.SaveCanvasCommand
import com.unifina.api.ValidationException
import com.unifina.domain.dashboard.Dashboard
import com.unifina.domain.security.Permission
import com.unifina.domain.security.SecUser
import com.unifina.domain.signalpath.Canvas
import com.unifina.exceptions.CanvasUnreachableException
import com.unifina.serialization.SerializationException
import com.unifina.signalpath.UiChannelIterator
import com.unifina.task.CanvasStartTask
import com.unifina.utils.Globals
import com.unifina.utils.GlobalsFactory
import com.unifina.utils.IdGenerator
import grails.converters.JSON
import grails.transaction.Transactional
import groovy.json.JsonBuilder
import groovy.transform.CompileStatic

class CanvasService {

	def grailsApplication
	def signalPathService
	def taskService
	def permissionService
	def dashboardService

	public List<Canvas> findAllBy(SecUser currentUser, String nameFilter, Boolean adhocFilter, Canvas.State stateFilter, String sort = "dateCreated", String order = "asc") {
		def query = Canvas.where { user == currentUser }

		if (nameFilter) {
			query = query.where {
				name == nameFilter
			}
		}
		if (adhocFilter != null) {
			query = query.where {
				adhoc == adhocFilter
			}
		}
		if (stateFilter) {
			query = query.where {
				state == stateFilter
			}
		}

		return query.order(sort, order).findAll()
	}


	public Map reconstruct(Canvas canvas) {
		Map signalPathMap = JSON.parse(canvas.json)
		return reconstructFrom(signalPathMap)
	}

	@CompileStatic
	public Canvas createNew(SaveCanvasCommand command, SecUser user) {
		Canvas canvas = new Canvas(user: user)
		updateExisting(canvas, command, true)
		return canvas
	}

	public void updateExisting(Canvas canvas, SaveCanvasCommand command, boolean resetUi = false) {
		if (!command.validate()) {
			throw new ValidationException(command.errors)
		}
		if (canvas.state != Canvas.State.STOPPED) {
			throw new InvalidStateException("Cannot update canvas with state " + canvas.state)
		}

		Map newSignalPathMap = constructNewSignalPathMap(canvas, command, resetUi)

		canvas.name = newSignalPathMap.name
		canvas.hasExports = newSignalPathMap.hasExports
		canvas.json = new JsonBuilder(newSignalPathMap).toString()
		canvas.state = Canvas.State.STOPPED
		canvas.adhoc = command.isAdhoc()

		// clear serialization
		canvas.serialized = null
		canvas.serializationTime = null

		canvas.save(flush: true, failOnError: true)
	}

	public void start(Canvas canvas, boolean clearSerialization) {
		if (canvas.state == Canvas.State.RUNNING) {
			throw new InvalidStateException("Cannot run canvas $canvas.id because it's already running. Stop it first.")
		}

		if (clearSerialization) {
			signalPathService.clearState(canvas)
		}

		try {
			signalPathService.startLocal(canvas, canvas.toMap().settings)
		} catch (SerializationException ex) {
			String msg = "Could not load (deserialize) previous state of canvas $canvas.id."
			throw new ApiException(500, "LOADING_PREVIOUS_STATE_FAILED", msg)
		}
	}

	public void startRemote(Canvas canvas, boolean forceReset=false, boolean resetOnError=true) {
		taskService.createTask(CanvasStartTask, CanvasStartTask.getConfig(canvas, forceReset, resetOnError), "canvas-start")
	}

	@Transactional(noRollbackFor=[CanvasUnreachableException])
	public void stop(Canvas canvas, SecUser user) throws ApiException {
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
	 */
	Map authorizedGetModuleOnCanvas(String canvasId, Integer moduleId, Long dashboardId, SecUser user, Permission.Operation op) {
		Canvas canvas = Canvas.get(canvasId)
		Dashboard dashboard

		if (!canvas) {
			throw new NotFoundException("Canvas", canvasId)
		} else if (!(hasCanvasPermission(canvas, user, op) ||
				dashboardId && (dashboard = dashboardService.authorizedGetById(dashboardId, user, op)) &&
				dashboard?.items?.find { it.canvas.id == canvasId && it.module == moduleId })) {
			throw new NotPermittedException(user?.username, "Canvas", canvasId, op.id)
		} else {
			Map canvasMap = canvas.toMap()
			Map moduleMap = canvasMap.modules.find { it.hash.toString() == moduleId?.toString() }

			if (!moduleMap) {
				throw new NotFoundException("Module $moduleId not found on canvas $canvasId", "Module", moduleId?.toString())
			}

			return moduleMap
		}
	}

	private boolean hasCanvasPermission(Canvas canvas, SecUser user, Permission.Operation op) {
		return op == Permission.Operation.READ && canvas.example || permissionService.check(user, canvas, op)
	}

	private Map constructNewSignalPathMap(Canvas canvas, SaveCanvasCommand command, boolean resetUi) {
		Map inputSignalPathMap = canvas.json != null ? JSON.parse(canvas.json) : [:]

		inputSignalPathMap.name = command.name
		inputSignalPathMap.modules = command.modules
		inputSignalPathMap.settings = command.settings

		if (resetUi) {
			resetUiChannels(inputSignalPathMap)
		}

		return reconstructFrom(inputSignalPathMap)
	}

	private static void resetUiChannels(Map signalPathMap) {
		HashMap<String,String> replacements = [:]
		UiChannelIterator.over(signalPathMap).each { UiChannelIterator.Element element ->
			if (replacements.containsKey(element.uiChannelData.id)) {
				element.uiChannelData.id = replacements[element.uiChannelData.id]
			}
			else {
				String newId = IdGenerator.get()
				replacements[element.uiChannelData.id] = newId
				element.uiChannelData.id = newId
			}
		}
	}

	/**
	 * Rebuild JSON to check it is ok and up-to-date
	 */
	private Map reconstructFrom(Map signalPathMap) {
		Globals globals = GlobalsFactory.createInstance(signalPathMap.settings ?: [:], grailsApplication)
		try {
			return signalPathService.reconstruct(signalPathMap, globals)
		} catch (Exception e) {
			throw e
		} finally {
			globals.destroy()
		}
	}

}