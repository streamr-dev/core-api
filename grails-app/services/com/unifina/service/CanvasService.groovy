package com.unifina.service

import com.unifina.domain.security.SecUser
import com.unifina.domain.signalpath.Canvas
import com.unifina.utils.Globals
import com.unifina.utils.GlobalsFactory
import grails.converters.JSON
import groovy.transform.CompileStatic
import org.codehaus.groovy.grails.web.json.JSONElement

class CanvasService {

	def grailsApplication
	def signalPathService

	public List<Canvas> findAllBy(SecUser currentUser,
								  String nameFilter,
								  Boolean adhocFilter,
								  Canvas.Type typeFilter,
								  Canvas.State stateFilter) {

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
		if (typeFilter) {
			query = query.where {
				type == typeFilter
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
	public Canvas createNew(JSONElement json, SecUser user) {
		Canvas canvas = new Canvas()
		updateExisting(canvas, json, user)
		return canvas
	}

	public void updateExisting(Canvas canvas, JSONElement json, SecUser user) {
		Map signalPathAsMap = reconstruct(json)
		def signalPathAsJson = (signalPathAsMap as JSON)

		canvas.name = signalPathAsMap.name
		canvas.hasExports = signalPathAsMap.hasExports
		canvas.adhoc = json.adhoc
		canvas.type = Canvas.Type.valueOf(json.type.toUpperCase())
		canvas.json = signalPathAsJson
		canvas.user = user
		canvas.save(flush: true, failOnError: true)
	}

	/**
	 * Rebuild JSON to check it is ok and up-to-date
 	 */
	private Map reconstruct(JSONElement json) {
		Globals globals = GlobalsFactory.createInstance(json.settings ?: [:], grailsApplication)
		try {
			return signalPathService.reconstruct(json, globals)
		} catch (Exception e) {
			throw e
		} finally {
			globals.destroy()
		}
	}
}
