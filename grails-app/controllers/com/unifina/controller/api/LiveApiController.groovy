package com.unifina.controller.api

import com.unifina.domain.security.SecUser
import com.unifina.domain.signalpath.RunningSignalPath
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
		except:['index',]
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
