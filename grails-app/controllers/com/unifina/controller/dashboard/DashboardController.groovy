package com.unifina.controller.dashboard

import com.unifina.domain.security.Permission.Operation
import com.unifina.domain.security.SecUser
import grails.converters.JSON
import grails.gorm.DetachedCriteria
import grails.plugin.springsecurity.annotation.Secured

import com.unifina.domain.dashboard.Dashboard
import com.unifina.domain.dashboard.DashboardItem

@Secured(["ROLE_USER"])
class DashboardController {
	def grailsApplication
	def springSecurityService
	def permissionService

	static defaultAction = "list"

	private def getAuthorizedDashboard(long id, Operation op=Operation.READ, boolean prefetchItems=false, Closure action) {
		SecUser user = springSecurityService.currentUser
		Dashboard dashboard = prefetchItems ? Dashboard.findById(params.id, [fetch:[items:"join"]]) : Dashboard.get(id);
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
		def dashboards = permissionService.get(Dashboard, user) { order "lastUpdated", "desc" }
		def shareable = permissionService.get(Dashboard, user, Operation.SHARE).toSet()
		def writable = permissionService.get(Dashboard, user, Operation.WRITE).toSet()
		return [dashboards:dashboards, shareable:shareable, writable:writable, user:user]
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
		getAuthorizedDashboard(params.long("id"), Operation.READ, true) { Dashboard dashboard, user ->
			render([
				id   : dashboard.id,
				name : dashboard.name,
				items: dashboard.items*.toMap()
			] as JSON)
		}
	}

	def show() {
		getAuthorizedDashboard(params.long("id")) { Dashboard dashboard, SecUser user ->
			return [
				serverUrl: grailsApplication.config.streamr.ui.server,
				dashboard: dashboard,
				shareable: permissionService.canShare(user, dashboard)
			]
		}
	}
	
	def delete() {
		getAuthorizedDashboard(params.long("id"), Operation.WRITE) { Dashboard dashboard, SecUser user ->
			// DashboardItems SHOULD be deleted because of belongsTo/hasMany, but it doesn't seem to work in 2.3.11
			new DetachedCriteria(DashboardItem).build {
				eq "dashboard", dashboard
			}.deleteAll()
			dashboard.delete(flush: true)
			flash.message = message(code: 'default.deleted.message', args: [message(code: 'dashboard.label', default: 'Dashboard'), dashboard.name])
			redirect(action: "list")
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
				toBeAdded.add(it)
			}
			toBeRemoved.each { dashboard.removeFromItems(it) }
			toBeAdded.each { dashboard.addToItems(it) }

			dashboard.save(flush: true, failOnError: true)
			render([success: true] as JSON)
		}
	}
}
