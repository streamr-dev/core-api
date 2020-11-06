package com.unifina.controller

import com.unifina.domain.*
import com.unifina.service.DashboardService
import com.unifina.service.SaveDashboardItemCommand
import grails.test.mixin.Mock
import grails.test.mixin.TestFor

@TestFor(DashboardItemApiController)
@Mock([Canvas, Dashboard, DashboardItem, User])
class DashboardItemApiControllerSpec extends ControllerSpecification {

	DashboardService dashboardService
	User me
	List<Dashboard> dashboards

	def setup() {
		dashboardService = controller.dashboardService = Mock(DashboardService)
		me = new User().save(failOnError: true, validate: false)
		dashboards = DashboardApiControllerSpec.initDashboards()

		Canvas c = new Canvas(json: '{"modules": [{"hash": 1, "uiChannel": {"webcomponent": "streamr-chart"}}]}')
		c.id = "canvas"
		c.save(failOnError: true, validate: false)
	}

	def "index() lists dashboard items"() {
		when:
		params.dashboardId = 3
		authenticatedAs(me) { controller.index() }

		then:
		response.status == 200
		response.json == [
			[
				id          : "2",
				dashboard   : dashboards[2].id,
				title       : "dashboard-3-item",
				canvas      : "1",
				module      : 2,
				webcomponent: "streamr-chart",
			],
			[
				id          : "3",
				dashboard   : dashboards[2].id,
				title       : "dashboard-3-item",
				canvas      : "1",
				module      : 3,
				webcomponent: "streamr-chart",
			]
		]
		1 * dashboardService.findById("3", me) >> dashboards[2]
		0 * dashboardService._
	}

	def "show() delegates to dashboardService.findDashboardItem()"() {
		when:
		params.dashboardId = 3
		params.id = 2
		authenticatedAs(me) { controller.show() }

		then:
		response.status == 200
		response.json == [
			id          : "2",
			dashboard   : dashboards[2].id,
			title       : "dashboard-3-item",
			canvas      : "1",
			module      : 2,
			webcomponent: "streamr-chart"
		]
		1 * dashboardService.findDashboardItem("3", "2", me) >> dashboards[2].items[0]
		0 * dashboardService._
	}

	def "save() delegates to dashboardService.addDashboardItem()"() {
		when:
		params.dashboardId = 3
		request.JSON = [
			title : "new-dashboard-item",
			canvas: "canvas",
			module: 1
		]
		authenticatedAs(me) { controller.save() }

		then:
		response.status == 200
		response.json == [
			id          : "32",
			dashboard   : "3",
			title       : "new-dashboard-item",
			canvas      : "canvas",
			module      : 1,
			webcomponent: "streamr-chart",
		]
		1 * dashboardService.addDashboardItem("3", _, me) >> { String dashboardId, SaveDashboardItemCommand command, User user ->
			def item = new DashboardItem(command.properties)
			item.dashboard = dashboards[2]
			item.id = "32"
			return item
		}
		0 * dashboardService._
	}

	def "update() delegates to dashboardService.updateDashboardItem()"() {
		when:
		params.dashboardId = 3
		params.id = 2
		request.JSON = [
			title : "updated-dashboard-item",
			canvas: "canvas",
			module: 1
		]
		authenticatedAs(me) { controller.update() }

		then:
		response.status == 200
		response.json == [
			id          : "2",
			dashboard   : "3",
			title       : "updated-dashboard-item",
			canvas      : "canvas",
			module      : 1,
			webcomponent: "streamr-chart"
		]
		1 * dashboardService.updateDashboardItem("3", "2", _, me) >> { String dashboardId, String itemId, SaveDashboardItemCommand command, User user ->
			def item = DashboardItem.get(itemId)
			item.setProperties(command.properties)
			return item
		}
		0 * dashboardService._
	}

	def "delete() delegates to dashboardService.deleteDashboardItem()"() {
		when:
		params.dashboardId = 3
		params.id = 2
		authenticatedAs(me) { controller.delete() }

		then:
		response.status == 204
		1 * dashboardService.deleteDashboardItem("3", "2", me)
		0 * dashboardService._
	}
}
