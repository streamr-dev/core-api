package com.unifina.controller.api

import com.unifina.domain.security.SecUser
import com.unifina.domain.signalpath.Canvas
import com.unifina.domain.signalpath.UiChannel
import com.unifina.security.StreamrApi
import com.unifina.signalpath.RuntimeResponse
import com.unifina.utils.GlobalsFactory
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured

@Secured(["IS_AUTHENTICATED_ANONYMOUSLY"])
class LiveApiController {

	def springSecurityService
	def unifinaSecurityService
	def signalPathService


	def beforeInterceptor = [
		action: {
			SecUser user = request.apiUser
			Canvas canvas = Canvas.get(params.long("id"))
			if (!unifinaSecurityService.canAccess(canvas, user)) {
				redirect(controller:'login', action:'ajaxDenied')
				return false
			}
			return true
		},
		except: ["index", "getModuleJson", "request", "ajaxCreate"],
	]

	@StreamrApi
	def index() {
		def canvases = Canvas.findAllByUserAndAdhoc(request.apiUser, false)
		List maps = canvases.collect { Canvas canvas ->
			[
				id: canvas.id,
				name: canvas.name,
				state: canvas.state,
				uiChannels: findUiChannels(canvas)
			]
		}
		render maps as JSON
	}

	def findUiChannels(Canvas canvas) {
		canvas.uiChannels.findAll { uiChannel ->
			uiChannel.module != null && uiChannel.module.webcomponent != null
		}.collect { uiChannel ->
			[
				id: uiChannel.id,
				name: uiChannel.name,
				module: [
					id: uiChannel.module.id,
					webcomponent: uiChannel.module.webcomponent
				]
			]
		}
	}

	@StreamrApi(requiresAuthentication = false)
	def getModuleJson() {
		response.setHeader('Access-Control-Allow-Origin', '*')

		UiChannel ui = UiChannel.findById(params.channel, [fetch: [runningSignalPath: 'join']])
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

	@StreamrApi
	def show() {
		// Access checked by beforeInterceptor
		Canvas canvas = Canvas.get(params.id)

		// Reconstruct as rsp.user
		Map signalPathData = JSON.parse(canvas.json)
		Map result = signalPathService.reconstruct(
			[signalPathData: signalPathData],
			GlobalsFactory.createInstance([live: true], grailsApplication, canvas.user)
		)

		result.runData = [
			uiChannels: canvas.uiChannels.collect { [id: it.id, hash: it.hash] },
			id: canvas.id
		]

		render result as JSON
	}

	@StreamrApi
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
			ui = UiChannel.findById(json?.channel, [fetch: [runningSignalPath: 'join']])
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

	@StreamrApi
	def ajaxCreate() {
		def signalPathData
		if (params.signalPathData) {
			signalPathData = JSON.parse(params.signalPathData);
		} else {
			signalPathData = JSON.parse(Canvas.get(params.id).json)
		}

		def signalPathContext =	JSON.parse(params.signalPathContext)

		Canvas canvas = signalPathService.createRunningCanvas(signalPathData, request.apiUser, signalPathContext.live ? false : true, true)
		signalPathService.startLocal(canvas, signalPathContext)

		Map result = [
			success: true,
			id: canvas.id,
			adhoc: canvas.adhoc,
			uiChannels: canvas.uiChannels.collect { [id: it.id, hash: it.hash] }
		]
		render result as JSON
	}

	@StreamrApi
	def ajaxStop() {
		Canvas rsp = Canvas.get(params.id)

		Map r
		if (rsp && signalPathService.stopLocal(rsp)) {
			r = [success:true, id:rsp.id, status:"Stopped"]
		} else {
			r = [success:false, id:params.id, status:"Running canvas not found"]
		}

		render r as JSON
	}
}
