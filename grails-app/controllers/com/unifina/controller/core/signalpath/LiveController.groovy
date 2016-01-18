package com.unifina.controller.core.signalpath

import com.unifina.domain.dashboard.DashboardItem
import com.unifina.domain.signalpath.Canvas
import com.unifina.domain.signalpath.UiChannel
import com.unifina.serialization.SerializationException
import com.unifina.signalpath.RuntimeResponse
import grails.plugin.springsecurity.annotation.Secured

class LiveController {
	
	def springSecurityService
	def unifinaSecurityService
	def signalPathService
	def grailsApplication
	
	def beforeInterceptor = [action:{
			if (!unifinaSecurityService.canAccess(Canvas.get(params.id))) {
				if (request.xhr) 
					redirect(controller:'login', action:'ajaxDenied')
				else 
					redirect(controller:'login', action:'denied')
					
				return false
			}
			else return true
		},
		except:['index','list', 'loadBrowser', 'loadBrowserContent']]
	
	@Secured("ROLE_USER")
	def index() {
		redirect(action:'list')
	}
	
	@Secured("ROLE_USER")
	def list() {
		List<Canvas> canvases = Canvas.createCriteria().list() {
			eq("user",springSecurityService.currentUser)
			eq("adhoc",false)
			if (params.term) {
				like("name","%${params.term}%")
			}
			
		}
		[running: canvases, user:springSecurityService.currentUser]
	}
	
	// Can be accessed anonymously for embedding the show view in iframes (eg. the landing page)
	@Secured("IS_AUTHENTICATED_ANONYMOUSLY")
	def show() {
		// Access checked by beforeInterceptor
		Canvas canvas = Canvas.get(params.id)
		
		// Ping the running SignalPath to check that it's alive
		def alive = canvas.state != Canvas.Type.RUNNING || signalPathService.ping(canvas, springSecurityService.currentUser)
		if (!alive) {
			flash.error = message(code:'runningSignalPath.ping.error')
		}

		[rsp:canvas]
	}

	@Secured("ROLE_USER")
	def start() {
		Canvas canvas = Canvas.get(params.id)
		if (params.clear) {
			signalPathService.clearState(canvas)
		}

		try {
			signalPathService.startLocal(canvas, [live: true])
			flash.message = message(code:"runningSignalPath.started", args:[canvas.name])
		} catch (SerializationException ex) {
			flash.error = message(code: "runningSignalPath.deserialization.error", args:[canvas.name])
			log.error("Failed to resume runningSignalPath " + canvas.id + " :", ex)
		}

		redirect(action:"show", id:canvas.id)
	}
	
	@Secured("ROLE_USER") 
	def stop() {
		Canvas canvas = Canvas.get(params.id)
		
		RuntimeResponse result = signalPathService.stopRemote(canvas, springSecurityService.currentUser)
		if (!result.isSuccess()) {
			log.error("stop: RSP $canvas.id could not be stopped due to: $result.error, marking RSP as stopped")
			flash.error = message(code:'runningSignalPath.stop.error')
			signalPathService.updateState(canvas.runner, Canvas.State.STOPPED)
		}
		else {
			flash.message = message(code:'runningSignalPath.stopped')
		}
		redirect(action:"show", id:canvas.id)
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
		def ssp = Canvas.executeQuery("select c.id, c.name from Canvas c where c.user = :user order by c.id desc",
			[user:springSecurityService.currentUser], [max: max, offset: offset])
		
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
		def canvasInstance = Canvas.get(params.id)
		if (canvasInstance) {
			try {
				def uicIds = UiChannel.executeQuery("SELECT uic.id FROM UiChannel uic WHERE uic.canvas =?", [canvasInstance])
				uicIds.each({String id ->
					DashboardItem.executeUpdate("DELETE FROM DashboardItem di WHERE di.uiChannel.id = ?", [id])
				})
				UiChannel.executeUpdate("delete from UiChannel uic where uic.canvas = ?", [canvasInstance])
				canvasInstance.delete(flush: true)
				flash.message = "${message(code: 'default.deleted.message', args: [message(code: 'runningSignalPath.label', default: 'RunningSignalPath'), canvasInstance.name])}"
				redirect(action: "list")
			}
			catch (org.springframework.dao.DataIntegrityViolationException e) {
				flash.message = "${message(code: 'default.not.deleted.message', args: [message(code: 'runningSignalPath.label', default: 'RunningSignalPath'), canvasInstance.name])}"
				redirect(action: "show", id: params.id)
			}
		}
		else {
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'runningSignalPath.label', default: 'RunningSignalPath'), params.id])}"
			redirect(action: "list")
		}
	}
}
