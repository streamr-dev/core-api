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
		Map dashboardMap = [
			id: dashboard.id,
			name: dashboard.name,
			items: dashboard.items.collect {item->
				[id:item.id, title: item.title, uiChannel: [id: item.uiChannel.id, name: item.uiChannel.name, module: [id:item.uiChannel.module.id]]]
			}
		]

		def allRunningSignalPaths = RunningSignalPath.findAllByUser(springSecurityService.currentUser)
		def runningSignalPaths = allRunningSignalPaths.findAll{RunningSignalPath rsp ->
			UiChannel found = rsp.uiChannels.find {UiChannel ui->
				ui.module
			}
			return found!=null
		}
		List runningSignalPathMaps = runningSignalPaths.collect {rsp->
			[
				id: rsp.id,
				name: rsp.name,
				uiChannels: rsp.uiChannels.findAll {it.module!=null}.collect {uiChannel->
					[id: uiChannel.id, name: uiChannel.name, module: [id:uiChannel.module.id]]
				}
			]
		}

		return [runningSignalPathsAsJson:(runningSignalPathMaps as JSON), dashboard:dashboard, dashboardAsJson:(dashboardMap as JSON), serverUrl: grailsApplication.config.streamr.ui.server]
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
		Map dashboardMap = request.JSON
		Dashboard dashboard = Dashboard.get(dashboardMap.id)
		
		dashboard.properties = dashboardMap
		
		// collect dashboard items into a map by id
		Map itemsById = [:]
		dashboard.items.findAll {it.id!=null}.each { 
			itemsById[it.id] = it
		}
		
		Collection toBeRemoved = []
		Collection toBeAdded = []
		
		
		dashboard.items.each {
			if(itemsById[it.id] == null){
				toBeRemoved.add(it)
			}
			else {
				it.properties = itemsById[it.id]
			}
		}
		
		dashboardMap.items.findAll {it.id==null}.each {
			DashboardItem item = new DashboardItem(it)
			//item.uiChannel = UiChannel.load(it.uiChannel.id)
			toBeAdded.add(it)
		}
		
		toBeRemoved.each {
			dashboard.removeFromItems(it)
		}
		
		toBeAdded.each {
			dashboard.addToItems(it)
		}
		
		dashboard.save(flush:true, failOnError:true)
		
		redirect(action: "edit", id: params.id)
	}
}
