package com.unifina.service

import com.unifina.api.SaveCanvasCommand
import com.unifina.api.ValidationException
import com.unifina.domain.security.SecUser
import com.unifina.domain.signalpath.Canvas
import com.unifina.domain.signalpath.UiChannel
import com.unifina.signalpath.UiChannelIterator
import com.unifina.utils.Globals
import com.unifina.utils.GlobalsFactory
import com.unifina.utils.IdGenerator
import grails.converters.JSON
import groovy.json.JsonBuilder
import groovy.transform.CompileStatic

class CanvasService {

	def grailsApplication
	def signalPathService

	public List<Canvas> findAllBy(SecUser currentUser, String nameFilter, Boolean adhocFilter, Canvas.State stateFilter) {
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

		return query.findAll()
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

		Map oldSignalPathMap = canvas.json != null ? JSON.parse(canvas.json) : null
		Map newSignalPathMap = constructNewSignalPathMap(canvas, command, resetUi)

		updateUiChannels(canvas, oldSignalPathMap, newSignalPathMap)

		canvas.name = newSignalPathMap.name
		canvas.hasExports = newSignalPathMap.hasExports
		canvas.json = new JsonBuilder(newSignalPathMap).toString()
		canvas.state = Canvas.State.STOPPED
		canvas.adhoc = command.isAdhoc()
		canvas.save(flush: true, failOnError: true)
	}

	public void start(Canvas canvas, boolean clearSerialization) {
		if (clearSerialization) {
			signalPathService.clearState(canvas)
		}
		signalPathService.startLocal(canvas, canvas.toMap().settings)
	}

	public void stop(Canvas canvas) {
		// TODO: handle return value
		boolean result = signalPathService.stopLocal(canvas)
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
		def foundIds = UiChannelIterator.over(newSignalPathMap).collect { UiChannelIterator.Element element ->
			if (UiChannel.findById(element.id) == null) {
				canvas.addToUiChannels(element.toUiChannel())
			}
			element.id
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
		UiChannelIterator.over(signalPathMap).each { UiChannelIterator.Element element ->
			element.uiChannelData.id = IdGenerator.get()
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