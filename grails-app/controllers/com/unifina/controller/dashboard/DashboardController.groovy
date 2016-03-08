package com.unifina.controller.dashboard

import com.unifina.domain.security.Permission.Operation
import com.unifina.domain.security.SecUser
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured

import com.unifina.domain.dashboard.Dashboard
import com.unifina.domain.dashboard.DashboardItem

@Secured(["ROLE_USER"])
class DashboardController {
	def grailsApplication
	def springSecurityService
	def permissionService

	static defaultAction = "list"

	private def getAuthorizedDashboard(long id, Operation op=Operation.READ, Closure action) {
		SecUser user = springSecurityService.currentUser
		Dashboard dashboard = Dashboard.get(id)
		if (!dashboard) {
			response.sendError(404)
			// TODO: alternative (from delete())
			//flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'dashboard.label', default: 'Dashboard'), params.id])}"
			//redirect(action: "list")
		} else if (!permissionService.check(user, dashboard, op)) {
			redirect controller: 'login', action: (request.xhr ? 'ajaxDenied' : 'denied')
		} else {
			action.call(dashboard, user)
		}
	}
	
	def list() {
		def user = springSecurityService.currentUser
		def dashboards = permissionService.getAll(Dashboard, user) { order "dateCreated", "desc" }
		def shareable = permissionService.getAll(Dashboard, user, Operation.SHARE)
		return [dashboards:dashboards, shareable:shareable, user:user]
	}
	
	def create() {
	}

	def save() {
		Dashboard dashboard = new Dashboard()
		dashboard.name = params.name
		dashboard.user = springSecurityService.currentUser
		dashboard.save(flush:true, failOnError:true)
		redirect(action:"show", id:dashboard.id)
	}
	
	def getJson() {
		// Here was: dashboard = Dashboard.findById(params.id, [fetch:[items:"join"]])
		// 			does this speed up things? Can there be a penalty for simply calling dashboard.items?
		getAuthorizedDashboard(params.long("id")) { Dashboard dashboard, user ->
			render([
				id   : dashboard.id,
				name : dashboard.name,
				items: dashboard.items.collect { DashboardItem item -> [
					id       : item.id,
					title    : item.title,
					ord      : item.ord,
					size     : item.size,
					canvas   : item.uiChannel.canvas.id,
					module   : item.uiChannel.hash,
					uiChannel: item.uiChannel.toMap()
				]}
			] as JSON)
		}
	}

	def show() {
		getAuthorizedDashboard(params.long("id")) { Dashboard dashboard, SecUser user ->
			return [serverUrl: grailsApplication.config.streamr.ui.server, dashboard: dashboard, shareable: permissionService.canShare(user, dashboard)]
		}
	}
	
	def delete() {
		getAuthorizedDashboard(params.long("id"), Operation.WRITE) { Dashboard dashboard, SecUser user ->
			try {
				DashboardItem.executeUpdate("delete from DashboardItem di where di.dashboard = ?", [dashboard])
				dashboard.delete(flush: true)
				flash.message = "${message(code: 'default.deleted.message', args: [message(code: 'dashboard.label', default: 'Dashboard'), dashboard.name])}"
				redirect(action: "list")
			} catch (org.springframework.dao.DataIntegrityViolationException e) {
				flash.message = "${message(code: 'default.not.deleted.message', args: [message(code: 'dashboard.label', default: 'Dashboard'), dashboard.name])}"
				redirect(action: "show", id: params.id)
			}
		}
	}
	
	def update() {
		Map dashboardMap = request.JSON
		getAuthorizedDashboard(dashboardMap.id, Operation.WRITE) { Dashboard dashboard, SecUser user ->
			dashboard.properties = dashboardMap

			// collect dashboard items into a map by id
			Map itemsById = [:]
			dashboard.items?.findAll { it.id != null }.each {
				itemsById[it.id] = it
			}

			Collection toBeRemoved = []
			Collection toBeAdded = []
			dashboard.items?.each {
				if (itemsById[it.id] == null) {
					toBeRemoved.add(it)
				} else {
					it.properties = itemsById[it.id]
				}
			}
			dashboardMap.items?.findAll { it.id == null }.each {
				DashboardItem item = new DashboardItem(it)
				//item.uiChannel = UiChannel.load(it.uiChannel.id)
				toBeAdded.add(it)
			}
			toBeRemoved.each { dashboard.removeFromItems(it) }
			toBeAdded.each { dashboard.addToItems(it) }

			dashboard.save(flush: true, failOnError: true)
			render([success: true] as JSON)
		}
	}
}
