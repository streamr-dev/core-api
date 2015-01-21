package com.unifina.controller.dashboard

import grails.plugin.springsecurity.annotation.Secured

import java.security.AccessControlException

import com.unifina.domain.dashboard.Dashboard
import com.unifina.domain.dashboard.DashboardItem
import com.unifina.domain.signalpath.RunningSignalPath
import com.unifina.domain.signalpath.UiChannel

@Secured(["ROLE_ADMIN"])
class DashboardController {
	def grailsApplication
	def springSecurityService
	
	def unifinaSecurityService
	def beforeInterceptor = [action:{unifinaSecurityService.canAccess(Dashboard.get(params.long("id")))},
		except:['list', 'create', 'save']]
	
	def list() {
		def dashboards = Dashboard.findAllByUser(springSecurityService.currentUser)
		return [dashboards:dashboards]
	}
	
	def create() {
		def runningSignalPaths = RunningSignalPath.findAllByUser(springSecurityService.currentUser)
		Dashboard dashboard = new Dashboard()
		return [runningSignalPaths:runningSignalPaths, dashboard:dashboard]
	}
	
	def save() {
		Dashboard dashboard = new Dashboard()
		dashboard.name = params.name
		dashboard.user = springSecurityService.currentUser
		
		List uiChannels = UiChannel.findAllByIdInList(params.list("uiChannels"))
	
		uiChannels.each {UiChannel uiChannel->
			if (!unifinaSecurityService.canAccess(uiChannel))
				throw new AccessControlException("Access to $uiChannel denied for user $springSecurityService.currentUser")
			
			DashboardItem item = new DashboardItem()
			item.uiChannel = uiChannel
			item.title = params["title_"+uiChannel.id]
			
			dashboard.addToItems(item)
		}
		
		dashboard.save(flush:true, failOnError:true)
		
		redirect(action: "show", id: dashboard.id)
	}
	
	def edit() {
		def runningSignalPaths = RunningSignalPath.findAllByUser(springSecurityService.currentUser)
		Dashboard dashboard = Dashboard.get(params.id)
		if (!dashboard) {
			flash.error = "Dashboard $params.id does not exist!"
			redirect(action:'list')
		}
		
		return [runningSignalPaths:runningSignalPaths, dashboard:dashboard]
	}
	
	def show() {
		Dashboard dashboard = Dashboard.get(params.id)
		return [serverUrl: grailsApplication.config.streamr.ui.server, dashboard:dashboard]
	}
}
