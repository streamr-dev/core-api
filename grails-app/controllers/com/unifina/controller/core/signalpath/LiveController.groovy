package com.unifina.controller.core.signalpath

import com.unifina.domain.dashboard.DashboardItem
import com.unifina.domain.security.SecUser
import com.unifina.domain.signalpath.Canvas
import com.unifina.domain.signalpath.UiChannel
import com.unifina.security.StreamrApi
import com.unifina.serialization.SerializationException
import com.unifina.signalpath.RuntimeResponse
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured

class LiveController {
	
	def springSecurityService
	def permissionService
	def signalPathService
	def grailsApplication
	
	def beforeInterceptor = [action:{
			if (!permissionService.canAccess(Canvas.get(params.id))) {
				if (request.xhr)
					redirect(controller:'login', action:'ajaxDenied')
				else
					redirect(controller:'login', action:'denied')

				return false
			}
			else return true
		},
		except:['index','list', 'loadBrowser', 'loadBrowserContent', 'request', 'getModuleJson']]

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
		def alive = canvas.state != Canvas.State.RUNNING || signalPathService.ping(canvas, springSecurityService.currentUser)
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

	// TODO: refactor out of here
	@StreamrApi(requiresAuthentication = false)
	@Secured("IS_AUTHENTICATED_ANONYMOUSLY")
	def getModuleJson() {
		response.setHeader('Access-Control-Allow-Origin', '*')

		UiChannel ui = UiChannel.findById(params.channel, [fetch: [canvas: 'join']])
		Canvas canvas = ui.canvas

		if (!permissionService.canAccess(canvas, request.apiUser)) {
			log.warn("request: access to ui ${ui?.id}, canvas ${canvas?.id} denied")
			render (status:403, text: [success:false, error: "User identified but not authorized to request this resource"] as JSON)
		} else {
			Map signalPathData = JSON.parse(canvas.json)
			Map moduleJson = signalPathData.modules.find { it.hash.toString() == ui.hash.toString() }

			if (!moduleJson) {
				render(status: 404, text: 'Module not found.')
			} else {
				render moduleJson as JSON
			}
		}
	}

	// TODO: refactor out of here
	@StreamrApi(requiresAuthentication = false)
	@Secured("IS_AUTHENTICATED_ANONYMOUSLY")
	def request() {
		Canvas canvas
		UiChannel ui = null
		Integer hash = null
		SecUser user = request.apiUser

		def json = request.JSON


		/**
		 * Provide as parameter:
		 * 1) Either the UI channel or Canvas.id & module.hash combo for messages intended for modules, or
		 * 2) Canvas.id for messages intended for the Canvas itself
		 */
		if (json?.channel) {
			ui = UiChannel.findById(json?.channel, [fetch: [canvas: 'join']])
			canvas = ui.canvas
			if (ui.hash)
				hash = Integer.parseInt(ui.hash)
		} else if (json?.id) {
			canvas = Canvas.get(json?.id)
			if (json?.hash != null)
				hash = json?.hash
		} else {
			log.warn("request: no channel and no id given. Request json: $json")
			render (status:400, text: [success:false, error: "Must give id and hash or channel in request"] as JSON)
		}
		
		if (!permissionService.canAccess(canvas, user)) {
			log.warn("request: access to rsp ${canvas?.id} denied for user ${user?.id}")
			render (status:403, text: [success:false, error: "User identified but not authorized to request this resource"] as JSON)
		} else {
			Map msg = json?.msg
			RuntimeResponse rr = signalPathService.runtimeRequest(msg, canvas, hash, user, json?.local ? true : false)

			log.info("request: responding with $rr")

			if (rr.containsKey("success") && rr.containsKey("response"))
				render rr as JSON
			else {
				Map result = [success: rr.isSuccess(), id: canvas.id, hash: hash, response: rr]
				render result as JSON
			}
		}
	}
}
