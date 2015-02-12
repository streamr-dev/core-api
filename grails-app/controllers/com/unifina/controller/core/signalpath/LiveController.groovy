package com.unifina.controller.core.signalpath

import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured

import java.security.AccessControlException

import com.unifina.domain.signalpath.RunningSignalPath
import com.unifina.domain.signalpath.SavedSignalPath

class LiveController {
	
	def springSecurityService
	def unifinaSecurityService
	def signalPathService
	
	static defaultAction = "list"
	
	def beforeInterceptor = [action:{unifinaSecurityService.canAccess(RunningSignalPath.get(params.long("id")))},
		except:['index','list','show','getJson', 'ajaxCreate']]
	
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
			render "Access denied"
		
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
			result.runData = [uiChannels:rsp.uiChannels.collect { [id:it.id, hash:it.hash] }, id: rsp.id]
			
			render result as JSON
		}
	}
	
	@Secured("ROLE_USER")
	def ajaxCreate() {
		def signalPathData
		if (params.signalPathData)
			signalPathData = JSON.parse(params.signalPathData);
		else signalPathData = JSON.parse(SavedSignalPath.get(Integer.parseInt(params.id)).json)

		def signalPathContext =	JSON.parse(params.signalPathContext)
		
		RunningSignalPath rsp = signalPathService.createRunningSignalPath(signalPathData, springSecurityService.currentUser, signalPathContext.live ? false : true, true)
		signalPathService.startLocal(rsp, signalPathContext)
		
		Map result = [success:true, id:rsp.id, uiChannels:rsp.uiChannels.collect { [id:it.id, hash:it.hash] }]
		render result as JSON
	}
	
	@Secured("ROLE_USER")
	def ajaxDelete() {
		RunningSignalPath rsp = RunningSignalPath.get(params.id)
		
		Map r
		if (rsp && signalPathService.stopLocal(rsp)) {
			r = [success:true, id:rsp.id, status:"Aborting"]
			rsp.delete(flush:true, failOnError:true)
		}
		else r = [success:false, id:rsp.id, status:"Running canvas not found"]
		
		render r as JSON
	}
	
	@Secured("ROLE_USER")
	def start() {
		RunningSignalPath rsp = RunningSignalPath.get(params.id)
		signalPathService.startLocal(rsp, [live:true])
		flash.message = message(code:"runningSignalPath.started", args:[rsp.name])
		redirect(action:"show", id:rsp.id)
	}
	
	@Secured("ROLE_USER") 
	def stop() {
		RunningSignalPath rsp = RunningSignalPath.get(params.id)
		if (signalPathService.stopLocal(rsp)) {
			flash.message = message(code:"runningSignalPath.stopped", args:[rsp.name])
			redirect(action:"show", id:rsp.id)
		}
		else {
			flash.error = "Error stopping Live Canvas $rsp.name. It might not be alive."
			redirect(action:"show", id:rsp.id)
		}
		
	}
}
