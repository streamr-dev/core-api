package com.unifina.service

import com.unifina.api.ApiException
import com.unifina.api.InvalidStateException
import com.unifina.api.SaveCanvasCommand
import com.unifina.api.ValidationException
import com.unifina.domain.security.SecUser
import com.unifina.domain.signalpath.Canvas
import com.unifina.domain.signalpath.UiChannel
import com.unifina.exceptions.CanvasUnreachableException
import com.unifina.serialization.SerializationException
import com.unifina.signalpath.UiChannelIterator
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

		Map oldSignalPathMap = canvas.json != null ? JSON.parse(canvas.json) : null
		Map newSignalPathMap = constructNewSignalPathMap(canvas, command, resetUi)

		updateUiChannels(canvas, oldSignalPathMap, newSignalPathMap)

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

	private void updateUiChannels(Canvas canvas, Map oldSignalPathMap, Map newSignalPathMap) {

		// Add new, previously unseen UiChannels
		HashSet<String> foundIds = new HashSet<>()
		UiChannelIterator.over(newSignalPathMap).each { UiChannelIterator.Element element ->
			if (canvas.uiChannels.find {it.id == element.id} == null && !foundIds.contains(element.id)) {
				canvas.addToUiChannels(element.toUiChannel())
			}
			foundIds.add(element.id)
		}

		// Remove no longer occurring UiChannels
		if (oldSignalPathMap != null) {
			UiChannelIterator.over(oldSignalPathMap).collect { UiChannelIterator.Element element ->
				if (!foundIds.contains(element.id)) {
					UiChannel uiChannel = canvas.uiChannels.find { it.id == element.id }
					if (uiChannel) {
						canvas.removeFromUiChannels(uiChannel)
						uiChannel.delete()
					}
				}
			}
		}
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