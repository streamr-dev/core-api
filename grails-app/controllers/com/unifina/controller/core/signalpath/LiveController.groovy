package com.unifina.controller.core.signalpath

import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured

import org.hibernate.StaleObjectStateException

import com.unifina.domain.dashboard.DashboardItem
import com.unifina.domain.security.SecUser
import com.unifina.domain.signalpath.RunningSignalPath
import com.unifina.domain.signalpath.SavedSignalPath
import com.unifina.domain.signalpath.UiChannel
import com.unifina.security.StreamrApi
import com.unifina.signalpath.RuntimeResponse

class LiveController {
	
	def springSecurityService
	def unifinaSecurityService
	def signalPathService
	
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
		except:['index','list','getListJson', 'ajaxCreate', 'loadBrowser', 'loadBrowserContent', 'request', 'getModuleJson']]
	
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
	
	@Secured("IS_AUTHENTICATED_ANONYMOUSLY")
	def getJson() {
		// Access checked by beforeInterceptor
		RunningSignalPath rsp = RunningSignalPath.get(params.id)

		Map signalPathData = JSON.parse(rsp.json)
		Map result = [:]
		
		result.signalPathData = signalPathData
		result.runData = [uiChannels:rsp.uiChannels.collect { [id:it.id, hash:it.hash] }, id: rsp.id]
		
		render result as JSON
	}
	
	@Secured("ROLE_USER")
	def getListJson() {
		def runningSignalPaths = RunningSignalPath.findAllByUserAndAdhoc(springSecurityService.currentUser, false)
		List runningSignalPathMaps = runningSignalPaths.collect {rsp->
			[
				id: rsp.id,
				name: rsp.name,
				state: rsp.state,
				uiChannels: rsp.uiChannels.collect {uiChannel->
					[id: uiChannel.id, name: uiChannel.name, module: (uiChannel.module ? [id:uiChannel.module.id] : null)]
				}
			]
		}
		render runningSignalPathMaps as JSON
	}

	@Secured("IS_AUTHENTICATED_ANONYMOUSLY")
	def getModuleJson() {
		response.setHeader('Access-Control-Allow-Origin', '*')
		
		UiChannel ui = UiChannel.findById(params.channel, [fetch: [runningSignalPath: 'join']])
		RunningSignalPath rsp = ui.runningSignalPath
		
		if (!unifinaSecurityService.canAccess(rsp)) {
			log.warn("request: access to ui ${ui?.id}, rsp ${rsp?.id} denied")
			render (status:403, text: [success:false, error: "User identified but not authorized to request this resource"] as JSON)
		}
		else {
			Map signalPathData = JSON.parse(rsp.json)
			Map moduleJson = signalPathData.modules.find { it.hash.toString() == ui.hash.toString() }
			
			if (!moduleJson) {
				render(status: 404, text: 'Module not found.')
			}
			else render moduleJson as JSON
		}
	}
	
	@StreamrApi(requiresAuthentication = false)
	@Secured("IS_AUTHENTICATED_ANONYMOUSLY")
	def request() {
		RunningSignalPath rsp
		UiChannel ui = null
		Integer hash = null
		SecUser user = request.apiUser ?: springSecurityService.currentUser
		
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
		}
		else if (json?.id) {
			rsp = RunningSignalPath.get(json?.id)
			if (json?.hash != null)
				hash = json?.hash
		}
		else {
			log.warn("request: no channel and no id given. Request json: $json")
			render (status:400, text: [success:false, error: "Must give id and hash or channel in request"] as JSON)
		}
		
		if (!unifinaSecurityService.canAccess(rsp, user)) {
			log.warn("request: access to rsp ${rsp?.id} denied for user ${user?.id}")
			render (status:403, text: [success:false, error: "User identified but not authorized to request this resource"] as JSON)
		}
		else {
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
		signalPathService.startLocal(rsp, [live:true])
		flash.message = message(code:"runningSignalPath.started", args:[rsp.name])
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
			tmp.url = createLink(controller:"live",action:"getJson",params:[id:it[0]])
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
