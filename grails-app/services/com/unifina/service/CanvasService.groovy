package com.unifina.service

import com.unifina.api.SaveCanvasCommand
import com.unifina.api.ValidationException
import com.unifina.domain.security.SecUser
import com.unifina.domain.signalpath.Canvas
import com.unifina.domain.signalpath.Module
import com.unifina.domain.signalpath.UiChannel
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

	@CompileStatic
	public Canvas createNew(SaveCanvasCommand command, SecUser user) {
		Canvas canvas = new Canvas(user: user)
		updateExisting(canvas, command)
		return canvas
	}

	public void updateExisting(Canvas canvas, SaveCanvasCommand command) {
		if (!command.validate()) {
			throw new ValidationException(command.errors)
		}
		Map signalPathAsMap = reconstructFrom(command.name, command.modules, command.settings)
		def signalPathAsJson = new JsonBuilder(signalPathAsMap).toString()

		canvas.name = signalPathAsMap.name
		canvas.hasExports = signalPathAsMap.hasExports
		canvas.state = Canvas.State.STOPPED
		canvas.json = signalPathAsJson
		canvas.save(flush: true, failOnError: true)
	}

	private void createAndAttachUiChannels(Canvas canvas) {

		// Main UiChannel
		UiChannel rspUi = new UiChannel()
		rspUi.id = IdGenerator.get()
		canvas.addToUiChannels(rspUi)

		// Modules' uiChannels
		for (Map it : sp.modules) {
			if (it.uiChannel) {
				UiChannel ui = new UiChannel()
				ui.id = it.uiChannel.id
				ui.hash = it.hash.toString()
				ui.module = Module.load(it.id)
				ui.name = it.uiChannel.name

				canvas.addToUiChannels(ui)
			}
		}
	}


	public Map reconstruct(Canvas canvas) {
		Map signalPathMap = JSON.parse(canvas.json)
		return reconstructFrom(signalPathMap.name, signalPathMap.modules, signalPathMap.settings)
	}

	/**
	 * Rebuild JSON to check it is ok and up-to-date
	 */
	private Map reconstructFrom(String name, List modules, Map settings) {
		Globals globals = GlobalsFactory.createInstance(settings ?: [:], grailsApplication)
		try {
			return signalPathService.reconstruct([name: name, modules: modules, settings: settings], globals)
		} catch (Exception e) {
			throw e
		} finally {
			globals.destroy()
		}
	}
}