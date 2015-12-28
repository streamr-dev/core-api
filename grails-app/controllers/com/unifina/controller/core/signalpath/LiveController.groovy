package com.unifina.controller.core.signalpath

import com.unifina.serialization.SerializationException
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured

import com.unifina.domain.dashboard.DashboardItem
import com.unifina.domain.security.SecUser
import com.unifina.domain.signalpath.RunningSignalPath
import com.unifina.domain.signalpath.SavedSignalPath
import com.unifina.domain.signalpath.UiChannel
import com.unifina.security.StreamrApi
import com.unifina.signalpath.RuntimeResponse
import com.unifina.utils.GlobalsFactory

class LiveController {
	
	def springSecurityService
	def unifinaSecurityService
	def signalPathService
	def grailsApplication
	
	def beforeInterceptor = [action:{
			if (!unifinaSecurityService.canAccess(RunningSignalPath.get(params.long("id")))) {
				if (request.xhr) 
					redirect(controller:'login', action:'ajaxDenied')
				else 
					redirect(controller:'login', action:'denied')
					
				return false
			}
			else return true
		},
		except:['index','list','getListJson', 'ajaxCreate', 'loadBrowser', 'loadBrowserContent']]
	
	@Secured("ROLE_USER")
	def index() {
		redirect(action:'list')
	}
	
	@Secured("ROLE_USER")
	def list() {
		List<RunningSignalPath> rsps = RunningSignalPath.createCriteria().list() {
			eq("user",springSecurityService.currentUser)
			eq("adhoc",false)
			if (params.term) {
				like("name","%${params.term}%")
			}
			
		}
		[running: rsps, user:springSecurityService.currentUser]
	}
	
	// Can be accessed anonymously for embedding the show view in iframes (eg. the landing page)
	@Secured("IS_AUTHENTICATED_ANONYMOUSLY")
	def show() {
		// Access checked by beforeInterceptor
		RunningSignalPath rsp = RunningSignalPath.get(params.id)
		
		// Ping the running SignalPath to check that it's alive
		def alive = rsp.state!='running' || signalPathService.ping(rsp, springSecurityService.currentUser)
		if (!alive)
			flash.error = message(code:'runningSignalPath.ping.error')
		
		[rsp:rsp]
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
		
		Map result = [success:true, id:rsp.id, adhoc:rsp.adhoc, uiChannels:rsp.uiChannels.collect { [id:it.id, hash:it.hash] }]
		render result as JSON
	}
	
	@Secured("ROLE_USER")
	def ajaxStop() {
		RunningSignalPath rsp = RunningSignalPath.get(params.id)
		
		Map r
		if (rsp && signalPathService.stopLocal(rsp)) {
			r = [success:true, id:rsp.id, status:"Stopped"]
		}
		else r = [success:false, id:params.id, status:"Running canvas not found"]
		
		render r as JSON
	}
	
	@Secured("ROLE_USER")
	def start() {
		RunningSignalPath rsp = RunningSignalPath.get(params.id)
		if (params.clear) {
			signalPathService.clearState(rsp)
		}

		try {
			signalPathService.startLocal(rsp, [live: true])
			flash.message = message(code:"runningSignalPath.started", args:[rsp.name])
		} catch (SerializationException ex) {
			flash.error = message(code: "runningSignalPath.deserialization.error", args:[rsp.name])
			log.error("failed to resume runningSignalPath", ex)
		}

		redirect(action:"show", id:rsp.id)
	}
	
	@Secured("ROLE_USER") 
	def stop() {
		RunningSignalPath rsp = RunningSignalPath.get(params.id)
		
		RuntimeResponse result = signalPathService.stopRemote(rsp, springSecurityService.currentUser)
		if (!result.isSuccess()) {
			log.error("stop: RSP $rsp.id could not be stopped due to: $result.error, marking RSP as stopped")
			flash.error = message(code:'runningSignalPath.stop.error')
			signalPathService.updateState(rsp.runner, "stopped")
		}
		else {
			flash.message = message(code:'runningSignalPath.stopped')
		}
		redirect(action:"show", id:rsp.id)
	}
	
	@Secured("ROLE_USER")
	def loadBrowser() {
		def result = [
			browserId: params.browserId,
			headers: ["Id", "Name"],
			contentUrl: createLink(
				controller: "live",
				action: "loadBrowserContent",
				params: [
					browserId: params.browserId,
					command: params.command
				]
			)
		]
		render(template:"/savedSignalPath/loadBrowser",model:result)
	}
	
	@Secured("ROLE_USER")
	def loadBrowserContent() {
		def max = params.int("max") ?: 100
		def offset = params.int("offset") ?: 0
		def ssp = SavedSignalPath.executeQuery("select sp.id, sp.name from RunningSignalPath sp where sp.user = :user order by sp.id desc", [user:springSecurityService.currentUser], [max: max, offset: offset])
		
		def result = [signalPaths:[]]
		ssp.each {
			def tmp = [:]
			tmp.id = it[0]
			tmp.name = it[1]
			tmp.url = createLink(controller:"liveApi",action:"show",params:[id:it[0]])
			tmp.command = params.command
			tmp.offset = offset++
			result.signalPaths.add(tmp)
		}
		render(view:"/savedSignalPath/loadBrowserContent",model:result)
	}
	
	@Secured("ROLE_USER")
	def delete() {
		def rspInstance = RunningSignalPath.get(params.id)
		if (rspInstance) {
			try {
				def uicIds = UiChannel.executeQuery("SELECT uic.id FROM UiChannel uic WHERE uic.runningSignalPath =?", [rspInstance])
				uicIds.each({String id ->
					DashboardItem.executeUpdate("DELETE FROM DashboardItem di WHERE di.uiChannel.id = ?", [id])
				})
				UiChannel.executeUpdate("delete from UiChannel uic where uic.runningSignalPath = ?", [rspInstance])
				rspInstance.delete(flush: true)
				flash.message = "${message(code: 'default.deleted.message', args: [message(code: 'runningSignalPath.label', default: 'RunningSignalPath'), rspInstance.name])}"
				redirect(action: "list")
			}
			catch (org.springframework.dao.DataIntegrityViolationException e) {
				flash.message = "${message(code: 'default.not.deleted.message', args: [message(code: 'runningSignalPath.label', default: 'RunningSignalPath'), rspInstance.name])}"
				redirect(action: "show", id: params.id)
			}
		}
		else {
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'runningSignalPath.label', default: 'RunningSignalPath'), params.id])}"
			redirect(action: "list")
		}
	}
}
