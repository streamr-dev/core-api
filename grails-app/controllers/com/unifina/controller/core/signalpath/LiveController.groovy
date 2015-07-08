package com.unifina.controller.core.signalpath

import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured

import com.unifina.domain.dashboard.DashboardItem
import com.unifina.domain.security.SecUser
import com.unifina.domain.signalpath.RunningSignalPath
import com.unifina.domain.signalpath.SavedSignalPath
import com.unifina.domain.signalpath.UiChannel
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
		except:['index','list','getListJson', 'ajaxCreate', 'loadBrowser', 'loadBrowserContent', 'request']]
	
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
			redirect(controller:'login', action:'ajaxDenied')
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
	
	@Secured("IS_AUTHENTICATED_ANONYMOUSLY")
	def request() {
		response.setHeader('Access-Control-Allow-Origin', '*')
		
		RunningSignalPath rsp
		UiChannel ui = null
		Integer hash = null
		
		/**
		 * Provide as parameter:
		 * 1) Either the UI channel or RSP.id & module.hash combo for messages intended for modules, or
		 * 2) RSP.id for messages intended for the RSP itself
		 */
		if (params.channel) {
			ui = UiChannel.findById(params.channel, [fetch: [runningSignalPath: 'join']])
			rsp = ui.runningSignalPath
			if (ui.hash)
				hash = Integer.parseInt(ui.hash)
		}
		else if (params.id) {
			rsp = RunningSignalPath.get(params.id)
			if (params.hash)
				hash = Integer.parseInt(params.hash)
		}
		
		if (!unifinaSecurityService.canAccess(rsp, params.auth)) {
			redirect(controller:'login', action:'ajaxDenied')
		}
		else {
			Map msg = JSON.parse(params.msg)
			SecUser user = springSecurityService.currentUser ?: (params.auth ? SecUser.findByApiKey(params.auth) : null)
			RuntimeResponse rr = signalPathService.runtimeRequest(msg, rsp, hash, user, servletContext, params.local ? true : false)
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
	def ajaxDelete() {
		RunningSignalPath rsp = RunningSignalPath.get(params.id)
		
		Map r
		if (rsp && signalPathService.stopLocal(rsp)) {
			r = [success:true, id:rsp.id, status:"Aborting"]
			rsp.delete(flush:true, failOnError:true)
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
		
		Map result = signalPathService.runtimeRequest([type:"stopRequest"], rsp, null, springSecurityService.currentUser, servletContext)
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
