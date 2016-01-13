package com.unifina.controller.api

import com.unifina.domain.security.SecUser
import com.unifina.domain.signalpath.RunningSignalPath
import com.unifina.domain.signalpath.SavedSignalPath
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
			RunningSignalPath rsp = RunningSignalPath.get(params.long("id"))
			if (!unifinaSecurityService.canAccess(rsp, user)) {
				redirect(controller:'login', action:'ajaxDenied')
				return false
			}
			return true
		},
		except: ["index", "getModuleJson", "request", "ajaxCreate"],
	]

	@StreamrApi
	def index() {
		def runningSignalPaths = RunningSignalPath.findAllByUserAndAdhoc(request.apiUser, false)
		List runningSignalPathMaps = runningSignalPaths.collect {RunningSignalPath rsp ->
			[
				id: rsp.id,
				name: rsp.name,
				state: rsp.state,
				uiChannels: rsp.uiChannels.collect { it.toMap() }
			]
		}
		render runningSignalPathMaps as JSON
	}

	@StreamrApi(requiresAuthentication = false)
	def getModuleJson() {
		response.setHeader('Access-Control-Allow-Origin', '*')

		UiChannel ui = UiChannel.findById(params.channel, [fetch: [runningSignalPath: 'join']])
		RunningSignalPath rsp = ui.runningSignalPath

		if (!unifinaSecurityService.canAccess(rsp, request.apiUser)) {
			log.warn("request: access to ui ${ui?.id}, rsp ${rsp?.id} denied")
			render (status:403, text: [success:false, error: "User identified but not authorized to request this resource"] as JSON)
		} else {
			Map signalPathData = JSON.parse(rsp.json)
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
		RunningSignalPath rsp = RunningSignalPath.get(params.id)

		// Reconstruct as rsp.user
		Map signalPathData = JSON.parse(rsp.json)
		Map result = signalPathService.reconstruct(
			[signalPathData: signalPathData],
			GlobalsFactory.createInstance([live: true], grailsApplication, rsp.user)
		)

		result.runData = [
			uiChannels: rsp.uiChannels.collect { [id: it.id, hash: it.hash] },
			id: rsp.id
		]

		render result as JSON
	}

	@StreamrApi
	def request() {
		RunningSignalPath rsp
		UiChannel ui = null
		Integer hash = null
		SecUser user = request.apiUser

		def json = request.JSON

		/**
		 * Provide as parameter:
		 * 1) Either the UI channel or RSP.id & module.hash combo for messages intended for modules, or
		 * 2) RSP.id for messages intended for the RSP itself
		 */
		if (json?.channel) {
			ui = UiChannel.findById(json?.channel, [fetch: [runningSignalPath: 'join']])
			rsp = ui.runningSignalPath
			if (ui.hash)
				hash = Integer.parseInt(ui.hash)
		} else if (json?.id) {
			rsp = RunningSignalPath.get(json?.id)
			if (json?.hash != null)
				hash = json?.hash
		} else {
			log.warn("request: no channel and no id given. Request json: $json")
			render (status:400, text: [success:false, error: "Must give id and hash or channel in request"] as JSON)
		}

		if (!unifinaSecurityService.canAccess(rsp, user)) {
			log.warn("request: access to rsp ${rsp?.id} denied for user ${user?.id}")
			render (status:403, text: [success:false, error: "User identified but not authorized to request this resource"] as JSON)
		} else {
			Map msg = json?.msg
			RuntimeResponse rr = signalPathService.runtimeRequest(msg, rsp, hash, user, json?.local ? true : false)

			log.info("request: responding with $rr")

			if (rr.containsKey("success") && rr.containsKey("response"))
				render rr as JSON
			else {
				Map result = [success: rr.isSuccess(), id: rsp.id, hash: hash, response: rr]
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
			signalPathData = JSON.parse(SavedSignalPath.get(Integer.parseInt(params.id)).json)
		}

		def signalPathContext =	JSON.parse(params.signalPathContext)

		RunningSignalPath rsp = signalPathService.createRunningSignalPath(signalPathData, request.apiUser, signalPathContext.live ? false : true, true)
		signalPathService.startLocal(rsp, signalPathContext)

		Map result = [
			success: true,
			id: rsp.id,
			adhoc: rsp.adhoc,
			uiChannels: rsp.uiChannels.collect { [id: it.id, hash: it.hash] }
		]
		render result as JSON
	}

	@StreamrApi
	def ajaxStop() {
		RunningSignalPath rsp = RunningSignalPath.get(params.id)

		Map r
		if (rsp && signalPathService.stopLocal(rsp)) {
			r = [success:true, id:rsp.id, status:"Stopped"]
		} else {
			r = [success:false, id:params.id, status:"Running canvas not found"]
		}

		render r as JSON
	}
}
