package com.unifina.service

import com.unifina.api.SaveCanvasCommand
import com.unifina.api.ValidationException
import com.unifina.domain.security.SecUser
import com.unifina.domain.signalpath.Canvas
import com.unifina.utils.Globals
import com.unifina.utils.GlobalsFactory
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
		Canvas canvas = new Canvas()
		updateExisting(canvas, command, user)
		return canvas
	}

	public void updateExisting(Canvas canvas, SaveCanvasCommand command, SecUser user) {
		if (!command.validate()) {
			throw new ValidationException(command.errors)
		}
		Map signalPathAsMap = reconstruct(command.name, command.modules, command.settings)
		def signalPathAsJson = new JsonBuilder(signalPathAsMap).toString()

		canvas.name = signalPathAsMap.name
		canvas.hasExports = signalPathAsMap.hasExports
		canvas.state = Canvas.State.STOPPED
		canvas.json = signalPathAsJson
		canvas.user = user
		canvas.save(flush: true, failOnError: true)
	}

	/**
	 * Rebuild JSON to check it is ok and up-to-date
	 */
	private Map reconstruct(String name, List modules, Map settings) {
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