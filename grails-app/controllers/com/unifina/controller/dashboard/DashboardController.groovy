package com.unifina.controller.dashboard

import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured

import com.unifina.domain.dashboard.Dashboard
import com.unifina.domain.dashboard.DashboardItem

@Secured(["ROLE_USER"])
class DashboardController {
	def grailsApplication
	def springSecurityService
	def unifinaSecurityService
	
	def beforeInterceptor = [action:{
			if (!unifinaSecurityService.canAccess(Dashboard.get(params.long("id")))) {
				if (request.xhr)
					redirect(controller:'login', action:'ajaxDenied')
				else
					redirect(controller:'login', action:'denied')
					
				return false
			}
			else return true
		},
		except:['list', 'create', 'save']]
	
	static defaultAction = "list"
	
	def list() {
		def dashboards = Dashboard.findAllByUser(springSecurityService.currentUser)
		return [dashboards:dashboards]
	}
	
	def create() {
		if (request.method=="GET")
			Dashboard dashboard = new Dashboard()
		else {
			Dashboard dashboard = new Dashboard()
			dashboard.name = params.name
			dashboard.user = springSecurityService.currentUser
			dashboard.save(flush:true, failOnError:true)
			redirect(action:"show", id:dashboard.id)
		}
		
	}
	
	def getJson() {
		Dashboard dashboard = Dashboard.findById(params.id, [fetch:[items:"join"]])
		Map dashboardMap = [
			id: dashboard.id,
			name: dashboard.name,
			items: dashboard.items.collect {DashboardItem item->
				[
						id:item.id,
						title: item.title,
						ord:item.ord,
						size:item.size,
						canvas: item.uiChannel.canvas.id,
						module: item.uiChannel.hash,
						uiChannel: item.uiChannel.toMap()
				]
			}
		]
		render dashboardMap as JSON
	}

	def show() {
		Dashboard dashboard = Dashboard.get(params.id)
		return [serverUrl: grailsApplication.config.streamr.ui.server, dashboard:dashboard]
	}
	
	def delete() {
		def dashboardInstance = Dashboard.get(params.id)
		if (dashboardInstance) {
			try {
				DashboardItem.executeUpdate("delete from DashboardItem di where di.dashboard = ?", [dashboardInstance])
				dashboardInstance.delete(flush: true)
				flash.message = "${message(code: 'default.deleted.message', args: [message(code: 'dashboard.label', default: 'Dashboard'), dashboardInstance.name])}"
				redirect(action: "list")
			}
			catch (org.springframework.dao.DataIntegrityViolationException e) {
				flash.message = "${message(code: 'default.not.deleted.message', args: [message(code: 'dashboard.label', default: 'Dashboard'), dashboardInstance.name])}"
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
		
		if (dashboard) {
			dashboard.properties = dashboardMap
			
			// collect dashboard items into a map by id
			Map itemsById = [:]
			dashboard.items?.findAll {it.id!=null}.each { 
				itemsById[it.id] = it
			}
			
			Collection toBeRemoved = []
			Collection toBeAdded = []
			
			
			dashboard.items?.each {
				if(itemsById[it.id] == null){
					toBeRemoved.add(it)
				}
				else {
					it.properties = itemsById[it.id]
				}
			}
			
			dashboardMap.items?.findAll {it.id==null}.each {
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
			
			render ([success:true] as JSON)
		}
		else {
			render(status: 404, text: "Dashboard $params.id not found!")
		}
	}
}
