package com.unifina.controller.api

import com.unifina.api.SaveDashboardItemCommand
import com.unifina.domain.dashboard.Dashboard
import com.unifina.domain.dashboard.DashboardItem
import com.unifina.domain.security.Key
import com.unifina.domain.security.SecUser
import com.unifina.domain.signalpath.Canvas
import com.unifina.filters.UnifinaCoreAPIFilters
import com.unifina.service.DashboardService
import com.unifina.service.UserService
import grails.plugin.springsecurity.SpringSecurityService
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import grails.test.mixin.web.FiltersUnitTestMixin
import groovy.json.JsonBuilder
import spock.lang.Specification

@TestFor(DashboardItemApiController)
@Mixin(FiltersUnitTestMixin)
@Mock([Canvas, Dashboard, DashboardItem, Key, SecUser, UnifinaCoreAPIFilters, UserService, SpringSecurityService])
class DashboardItemApiControllerSpec extends Specification {

	DashboardService dashboardService
	SecUser me
	List<Dashboard> dashboards

	def setup() {
		dashboardService = controller.dashboardService = Mock(DashboardService)
		me = new SecUser().save(failOnError: true, validate: false)
		dashboards = DashboardApiControllerSpec.initDashboards(me)

		Key key = new Key(name: "key", user: me)
		key.id = "myApiKey"
		key.save(failOnError: true, validate: true)

		Canvas c = new Canvas(json: '{"modules": [{"hash": 1, "uiChannel": {"webcomponent": "streamr-chart"}}]}')
		c.id = "canvas"
		c.save(failOnError: true, validate: false)
	}

	def "index() lists dashboard items"() {
		when:
		params.dashboardId = 3
		request.addHeader("Authorization", "Token myApiKey")
		request.requestURI = "/api/v1/dashboards/3/items"
		withFilters(action: "index") {
			controller.index()
		}

		then:
		response.status == 200
		response.json == [
			[
				id: 3,
				dashboard: dashboards[2].id,
				title: "dashboard-3-item-2",
				size: "x-large",
				canvas: "1",
				module: 3,
				webcomponent: "streamr-chart",
				ord: 0,
			],
			[
				id: 2,
				dashboard: dashboards[2].id,
				title: "dashboard-3-item-1",
				size: "small",
				canvas: "1",
				module: 2,
				webcomponent: "streamr-table",
				ord: 1,
			],
		]
		1 * dashboardService.findById(3L, me) >> dashboards[2]
		0 * dashboardService._
	}

	def "show() delegates to dashboardService.findDashboardItem()"() {
		when:
		params.dashboardId = 3
		params.id = 2
		request.addHeader("Authorization", "Token myApiKey")
		request.requestURI = "/api/v1/dashboards/3/items/2"
		withFilters(action: "index") {
			controller.show()
		}

		then:
		response.status == 200
		response.json == [
			id: 2,
			dashboard: dashboards[2].id,
			title: "dashboard-3-item-1",
			size: "small",
			canvas: "1",
			module: 2,
			webcomponent: "streamr-table",
			ord: 1,
		]
		1 * dashboardService.findDashboardItem(3L, 2L, me) >> dashboards[2].items[1]
		0 * dashboardService._
	}

	def "save() delegates to dashboardService.addDashboardItem()"() {
		when:
		params.dashboardId = 3
		request.JSON = [
			title: "new-dashboard-item",
			canvas: "canvas",
			module: 1,
			ord: 3,
			size: "small",
		]
		request.addHeader("Authorization", "Token myApiKey")
		request.requestURI = "/api/v1/dashboards/3/items/"
		withFilters(action: "save") {
			controller.save()
		}

		then:
		response.status == 200
		response.json == [
			id: 32,
			dashboard: 3,
			title: "new-dashboard-item",
			ord: 3,
			canvas: "canvas",
			module: 1,
			webcomponent: "streamr-chart",
			size: "small",
		]
		1 * dashboardService.addDashboardItem(3, _, me) >> { Long dashboardId,
															 SaveDashboardItemCommand command,
															 SecUser user ->
			def item = command.toDashboardItem()
			item.dashboard = dashboards[2]
			item.id = 32
			return item
		}
		0 * dashboardService._
	}

	def "update() delegates to dashboardService.updateDashboardItem()"() {
		when:
		params.dashboardId = 2
		params.id = 1
		request.JSON = [
			title	: "updated-dashboard-item",
			canvas	: "canvas",
			module	: 1,
			ord		: 9,
			size	: "large",
		]
		request.addHeader("Authorization", "Token myApiKey")
		request.requestURI = "/api/v1/dashboards/3/items/1"
		withFilters(action: "update") {
			controller.update()
		}

		then:
		response.status == 200
		response.json == [
			id           : 1,
			dashboard	 : 2,
			title        : "updated-dashboard-item",
			ord          : 9,
			canvas       : "canvas",
			module       : 1,
			webcomponent : "streamr-chart",
			size         : "large",
		]
		1 * dashboardService.updateDashboardItem(2, 1, _, me) >> { Long dashboardId,
																   Long itemId,
																   SaveDashboardItemCommand command,
																   SecUser user ->
			def item = DashboardItem.get(itemId)
			command.copyValuesTo(item)
			return item
		}
		0 * dashboardService._
	}

	def "delete() delegates to dashboardService.deleteDashboardItem()"() {
		when:
		params.dashboardId = 3
		params.id = 2
		request.addHeader("Authorization", "Token myApiKey")
		request.requestURI = "/api/v1/dashboards/3/items/2"
		withFilters(action: "delete") {
			controller.delete()
		}

		then:
		response.status == 204
		1 * dashboardService.deleteDashboardItem(3, 2, me)
		0 * dashboardService._
	}
}
