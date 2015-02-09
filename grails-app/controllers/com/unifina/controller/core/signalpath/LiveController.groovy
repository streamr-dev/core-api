package com.unifina.controller.core.signalpath

import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured

import java.security.AccessControlException

import com.unifina.domain.signalpath.RunningSignalPath

class LiveController {
	
	def springSecurityService
	def unifinaSecurityService
	
	static defaultAction = "list"
	
	@Secured("ROLE_USER")
	def list() {
		List<RunningSignalPath> rsps = RunningSignalPath.createCriteria().list() {
			eq("user",springSecurityService.currentUser)
			if (params.term) {
				like("name","%${params.term}%")
			}
		}
		[running: rsps, user:springSecurityService.currentUser]
	}
	
	@Secured("IS_AUTHENTICATED_ANONYMOUSLY")
	def show() {
		RunningSignalPath rsp = RunningSignalPath.get(params.id)
		if (!unifinaSecurityService.canAccess(rsp))
			throw new AccessControlException("Access denied")
		
		[rsp:rsp]
	}
	
	@Secured("IS_AUTHENTICATED_ANONYMOUSLY")
	def getJson() {
		RunningSignalPath rsp = RunningSignalPath.get(params.id)
		if (!unifinaSecurityService.canAccess(rsp)) {
			Map err = [error: "Access denied."]
			render err as JSON
		}
		else {
			Map signalPathData = JSON.parse(rsp.json)
			Map result = [:]
			
			result.signalPathData = signalPathData
			result.runData = [uiChannels:rsp.uiChannels.collect { [id:it.id, hash:it.hash] }, runnerId:rsp.runner]
			
			render result as JSON
		}
	}
}
