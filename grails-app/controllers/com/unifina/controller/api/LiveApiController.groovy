package com.unifina.controller.api

import com.unifina.domain.security.SecUser
import com.unifina.domain.signalpath.RunningSignalPath
import com.unifina.domain.signalpath.UiChannel
import com.unifina.security.StreamrApi
import com.unifina.security.TokenAuthenticator
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
		except:['index', 'getModuleJson']
	]

	@StreamrApi
	def index() {
		def runningSignalPaths = RunningSignalPath.findAllByUserAndAdhoc(request.apiUser, false)
		List runningSignalPathMaps = runningSignalPaths.collect {RunningSignalPath rsp ->
			[
				id: rsp.id,
				name: rsp.name,
				state: rsp.state,
				uiChannels: rsp.uiChannels.collect {uiChannel ->
					[
						id: uiChannel.id,
					 	name: uiChannel.name,
						module: (uiChannel.module ? [id:uiChannel.module.id] : null)
					]
				}
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
}
