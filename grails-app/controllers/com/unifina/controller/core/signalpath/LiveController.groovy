package com.unifina.controller.core.signalpath

import com.unifina.domain.dashboard.DashboardItem
import com.unifina.domain.security.SecUser
import com.unifina.domain.signalpath.Canvas
import com.unifina.domain.signalpath.UiChannel
import com.unifina.security.StreamrApi
import com.unifina.serialization.SerializationException
import com.unifina.signalpath.RuntimeResponse
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured

class LiveController {
	
	def springSecurityService
	def unifinaSecurityService
	def signalPathService
	def grailsApplication

	// TODO: refactor out of here
	@StreamrApi(requiresAuthentication = false)
	@Secured("IS_AUTHENTICATED_ANONYMOUSLY")
	def getModuleJson() {
		UiChannel ui = UiChannel.findById(params.channel, [fetch: [canvas: 'join']])
		Canvas canvas = ui.canvas

		if (!unifinaSecurityService.canAccess(canvas, request.apiUser)) {
			log.warn("request: access to ui ${ui?.id}, canvas ${canvas?.id} denied")
			render (status:403, text: [success:false, error: "User identified but not authorized to request this resource"] as JSON)
		} else {
			Map signalPathData = JSON.parse(canvas.json)
			Map moduleJson = signalPathData.modules.find { it.hash.toString() == ui.hash.toString() }

			if (!moduleJson) {
				render(status: 404, text: 'Module not found.')
			} else {
				render moduleJson as JSON
			}
		}
	}

	// TODO: refactor out of here
	@StreamrApi(requiresAuthentication = false)
	@Secured("IS_AUTHENTICATED_ANONYMOUSLY")
	def request() {
		Canvas canvas
		UiChannel ui = null
		Integer hash = null
		SecUser user = request.apiUser

		def json = request.JSON


		/**
		 * Provide as parameter:
		 * 1) Either the UI channel or Canvas.id & module.hash combo for messages intended for modules, or
		 * 2) Canvas.id for messages intended for the Canvas itself
		 */
		if (json?.channel) {
			ui = UiChannel.findById(json?.channel, [fetch: [canvas: 'join']])
			canvas = ui.canvas
			if (ui.hash)
				hash = Integer.parseInt(ui.hash)
		} else if (json?.id) {
			canvas = Canvas.get(json?.id)
			if (json?.hash != null)
				hash = json?.hash
		} else {
			log.warn("request: no channel and no id given. Request json: $json")
			render (status:400, text: [success:false, error: "Must give id and hash or channel in request"] as JSON)
		}

		if (!unifinaSecurityService.canAccess(canvas, user)) {
			log.warn("request: access to rsp ${canvas?.id} denied for user ${user?.id}")
			render (status:403, text: [success:false, error: "User identified but not authorized to request this resource"] as JSON)
		} else {
			Map msg = json?.msg
			RuntimeResponse rr = signalPathService.runtimeRequest(msg, canvas, hash, user, json?.local ? true : false)

			log.info("request: responding with $rr")

			if (rr.containsKey("success") && rr.containsKey("response"))
				render rr as JSON
			else {
				Map result = [success: rr.isSuccess(), id: canvas.id, hash: hash, response: rr]
				render result as JSON
			}
		}
	}
}
