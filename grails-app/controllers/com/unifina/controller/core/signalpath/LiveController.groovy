package com.unifina.controller.core.signalpath

import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured

import java.security.AccessControlException

import com.unifina.domain.signalpath.RunningSignalPath

@Secured("ROLE_USER")
class LiveController {
	
	def springSecurityService
	def unifinaSecurityService
	
	static defaultAction = "list"
	
	def list() {
		List<RunningSignalPath> rsps = RunningSignalPath.createCriteria().list() {
			eq("user",springSecurityService.currentUser)
			if (params.term) {
				like("name","%${params.term}%")
			}
		}
		[running: rsps, user:springSecurityService.currentUser]
	}
	
	def show() {
		RunningSignalPath rsp = RunningSignalPath.get(params.id)
		if (!unifinaSecurityService.canAccess(rsp))
			throw new AccessControlException("Access denied")
		
		[rsp:rsp]
	}
	
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
