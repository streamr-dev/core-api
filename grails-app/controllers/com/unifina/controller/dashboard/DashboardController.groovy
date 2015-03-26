package com.unifina.controller.dashboard

import grails.converters.JSON
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
	
	static defaultAction = "list"
	
	def list() {
		def dashboards = Dashboard.findAllByUser(springSecurityService.currentUser)
		return [dashboards:dashboards]
	}
	
	def create() {
		def allRunningSignalPaths = RunningSignalPath.findAllByUser(springSecurityService.currentUser)
		def runningSignalPaths = allRunningSignalPaths.findAll{RunningSignalPath rsp ->
			UiChannel found = rsp.uiChannels.find {UiChannel ui->
				ui.module
			}
			return found!=null
		}
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
		Dashboard dashboard = Dashboard.findById(params.id, [fetch:[items:"join"]])
		
		def allRunningSignalPaths = RunningSignalPath.findAllByUser(springSecurityService.currentUser)
		def runningSignalPaths = allRunningSignalPaths.findAll{RunningSignalPath rsp ->
			UiChannel found = rsp.uiChannels.find {UiChannel ui->
				ui.module
			}
			return found!=null
		}

		
		
		return [runningSignalPathsAsJson:(runningSignalPaths as JSON), dashboard:dashboard, dashboardAsJson:(dashboard as JSON), dashboardItemsAsJson:(dashboard.items as JSON), serverUrl: grailsApplication.config.streamr.ui.server]
	}
	
	def show() {
		Dashboard dashboard = Dashboard.get(params.id)
		return [serverUrl: grailsApplication.config.streamr.ui.server, dashboard:dashboard]
	}
	
	def delete = {
		def dashboardInstance = Dashboard.get(params.id)
		if (dashboardInstance) {
			try {
				dashboardInstance.delete(flush: true)
				flash.message = "${message(code: 'default.deleted.message', args: [message(code: 'dashboard.label', default: 'Dashboard'), params.id])}"
				redirect(action: "list")
			}
			catch (org.springframework.dao.DataIntegrityViolationException e) {
				flash.message = "${message(code: 'default.not.deleted.message', args: [message(code: 'dashboard.label', default: 'Dashboard'), params.id])}"
				redirect(action: "show", id: params.id)
			}
		}
		else {
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'dashboard.label', default: 'Dashboard'), params.id])}"
			redirect(action: "list")
		}
	}
	
	def update() {
		Dashboard dashboard = Dashboard.get(params.id)
		dashboard.name = params.name
		
		List uiChannels = UiChannel.findAllByIdInList(params.list("uiChannels"))

		Collection added = uiChannels.findAll {UiChannel ui->
			DashboardItem found = dashboard.items.find {DashboardItem item-> 
				item.uiChannel.id == ui.id
			}
			return found==null
		}
		
		Collection removed = dashboard.items.findAll {DashboardItem item->
			UiChannel found = uiChannels.find {UiChannel ui->
				item.uiChannel.id == ui.id				
			}
			return found==null
		}
			
		added.each {UiChannel uiChannel->
			if (!unifinaSecurityService.canAccess(uiChannel))
				throw new AccessControlException("Access to $uiChannel denied for user $springSecurityService.currentUser")
			
			DashboardItem item = new DashboardItem()
			item.uiChannel = uiChannel
			
			dashboard.addToItems(item)
		}
		
		removed.each {DashboardItem item->			
			dashboard.removeFromItems(item)
		}
		
		// Set the titles of all DashboardItems
		dashboard.items.each {DashboardItem item->
			item.title = params["title_"+item.uiChannel.id]
		}
		
		dashboard.save(flush:true, failOnError:true)
		
		redirect(action: "show", id: dashboard.id)
	}
}
