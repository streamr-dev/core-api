package com.unifina.controller.core.signalpath

import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured

import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

import com.unifina.domain.signalpath.RunningSignalPath
import com.unifina.domain.signalpath.SavedSignalPath
import com.unifina.domain.signalpath.UiChannel
import com.unifina.signalpath.SignalPath
import com.unifina.signalpath.SignalPathRunner

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
			render(status: 401, text: 'Access denied.')
		
		[rsp:rsp]
	}
	
	@Secured("IS_AUTHENTICATED_ANONYMOUSLY")
	def getJson() {
		RunningSignalPath rsp = RunningSignalPath.get(params.id)
		if (!unifinaSecurityService.canAccess(rsp)) {
			render(status: 401, text: 'Access denied.')
		}
		else {
			Map signalPathData = JSON.parse(rsp.json)
			Map result = [:]
			
			result.signalPathData = signalPathData
			result.runData = [uiChannels:rsp.uiChannels.collect { [id:it.id, hash:it.hash] }, id: rsp.id]
			
			render result as JSON
		}
	}
	
	@Secured("IS_AUTHENTICATED_ANONYMOUSLY")
	def getModuleJson() {
		response.setHeader('Access-Control-Allow-Origin', '*')
		
		UiChannel ui = UiChannel.findById(params.channel, [fetch: [runningSignalPath: 'join']])
		RunningSignalPath rsp = ui.runningSignalPath
		
		if (!unifinaSecurityService.canAccess(rsp)) {
			render(status: 401, text: 'Access denied.')
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
	def uiAction() {
		response.setHeader('Access-Control-Allow-Origin', '*')
		
		UiChannel ui = UiChannel.findById(params.channel, [fetch: [runningSignalPath: 'join']])
		RunningSignalPath rsp = ui.runningSignalPath
		Map msg = JSON.parse(params.msg)
		Integer hash = Integer.parseInt(ui.hash)
		
		if (!unifinaSecurityService.canAccess(rsp)) {
			render(status: 401, text: 'Access denied.')
		}
		else {
			SignalPathRunner spr = servletContext["signalPathRunners"]?.get(rsp.runner)

			// Give an error if the runner was not found locally although it should have been 
			if (params.local && !spr) {
				Map err = [success:false, channel:params.channel, error: "Canvas not found!"]
				render err as JSON
			}
			// May be a remote runner, check server and send a message
			else if (!params.local && !spr) {
				// TODO: implement
				Map err = [success:false, channel:params.channel, error: "Canvas not found!"]
				render err as JSON
			}
			// If runner found
			else {
				SignalPath sp = spr.signalPaths.find {
					it.runningSignalPath.id == rsp.id
				}
				
				if (!sp) {
					Map err = [success:false, channel:params.channel, error: "Canvas not found in runner. This should not happen."]
					render err as JSON
				}
				else {
					Map	r = [success:true, channel:params.channel, msg:msg]
					
					Future future = sp.getModule(hash).receiveUIMessage(msg)
					if (future) {
						try {
							r.response = future.get(20, TimeUnit.SECONDS)
							render r as JSON
						} catch (TimeoutException e) {
							Map err = [success:false, channel:params.channel, error: "Timed out while waiting for response."]
							render err as JSON
						}
					}			
					
				}
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
