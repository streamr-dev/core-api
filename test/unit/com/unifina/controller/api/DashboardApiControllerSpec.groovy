package com.unifina.controller.api

import com.unifina.api.DashboardListParams
import com.unifina.api.ListParams
import com.unifina.api.SaveDashboardCommand
import com.unifina.api.ValidationException
import com.unifina.domain.dashboard.Dashboard
import com.unifina.domain.dashboard.DashboardItem
import com.unifina.domain.security.Key
import com.unifina.domain.security.SecUser
import com.unifina.domain.signalpath.Canvas
import com.unifina.filters.UnifinaCoreAPIFilters
import com.unifina.service.ApiService
import com.unifina.service.DashboardService
import com.unifina.service.UserService
import com.unifina.utils.Webcomponent
import grails.plugin.springsecurity.SpringSecurityService
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import spock.lang.Specification

@TestFor(DashboardApiController)
@Mock([Canvas, Dashboard, DashboardItem, Key, SecUser, UnifinaCoreAPIFilters])
class DashboardApiControllerSpec extends Specification {

	ApiService apiService
	DashboardService dashboardService
	SecUser me
	List<Dashboard> dashboards

	// This gets the real services injected into the filters
	// From https://github.com/grails/grails-core/issues/9191
	static doWithSpring = {
		springSecurityService(SpringSecurityService)
		userService(UserService)
	}

	def setup() {
		dashboardService = controller.dashboardService = Mock(DashboardService)
		controller.apiService = apiService = Mock(ApiService)

		me = new SecUser().save(failOnError: true, validate: false)
		dashboards = initDashboards()

		def key = new Key(user: me, name: "my key")
		key.id = "myApiKey"
		key.save(failOnError: true, validate: true)
	}

	static List<Dashboard> initDashboards() {
		List<Dashboard> dashboards = []

		Canvas canvas = new Canvas()
		canvas.save(failOnError: true, validate: false)

		[1, 2, 3].each {
			Dashboard dashboard = new Dashboard(name: "dashboard-${it}")
			dashboard.id = it.toString()
			dashboards.add(dashboard)
			dashboard.save()
		}

		[2, 3].each {
			DashboardItem item = new DashboardItem(
					title: "dashboard-3-item",
					canvas: canvas,
					module: it,
					webcomponent: Webcomponent.STREAMR_CHART,
					dashboard: dashboards[2]
			)
			item.id = it.toString()
			item.save()
			dashboards[2].addToItems(item)
		}

		return dashboards
	}

	void "index() renders authorized dashboards as a list"() {
		when:
		request.addHeader("Authorization", "Token myApiKey")
		request.requestURI = "/api/v1/dashboards"
		withFilters(action: "index") {
			controller.index()
		}

		then:
		response.status == 200
		response.json.size() == 3
		1 * controller.apiService.list(Dashboard, _, me) >> { clazz, ListParams listParams, user ->
			assert listParams.toMap() == new DashboardListParams().toMap()
			dashboards
		}
	}

	void "index() adds name param to filter criteria"() {
		when:
		params.name = "Foo"
		request.addHeader("Authorization", "Token myApiKey")
		request.requestURI = "/api/v1/dashboards"
		withFilters(action: "index") {
			controller.index()
		}

		then:
		response.status == 200
		response.json.size() == 0
		1 * controller.apiService.list(Dashboard, _, me) >> { clazz, ListParams listParams, user ->
			assert listParams.toMap() == new DashboardListParams(name: "Foo").toMap()
			[]
		}
	}

	def "show() shows dashboard with 0 items"() {
		when:
		params.id = "1"
		request.addHeader("Authorization", "Token myApiKey")
		request.requestURI = "/api/v1/dashboards"
		withFilters(action: "show") {
			controller.show()
		}

		then:
		response.status == 200

		response.json == [
				id    : "1",
				items : [],
				name  : "dashboard-1",
				layout: [:]
		]

		1 * dashboardService.findById("1", me) >> dashboards[0]
		0 * dashboardService._
	}

	def "show() shows dashboard with many items"() {
		when:
		params.id = "3"
		request.addHeader("Authorization", "Token myApiKey")
		request.requestURI = "/api/v1/dashboards"
		withFilters(action: "show") {
			controller.show()
		}

		then:
		response.status == 200
		response.json == [
				id    : "3",
				items : [
						[
								id          : "2",
								dashboard   : dashboards[2].id,
								title       : "dashboard-3-item",
								canvas      : "1",
								module      : 2,
								webcomponent: "streamr-chart"
						],
						[
								id          : "3",
								dashboard   : dashboards[2].id,
								title       : "dashboard-3-item",
								canvas      : "1",
								module      : 3,
								webcomponent: "streamr-chart"
						],
				],
				layout: [:],
				name  : "dashboard-3"
		]
		1 * dashboardService.findById("3", me) >> dashboards[2]
		0 * dashboardService._
	}

	def "save() throws ValidationException given incomplete json"() {
		when:
		request.addHeader("Authorization", "Token myApiKey")
		request.JSON = [
				name: "",
		]
		request.requestURI = "/api/v1/dashboards"
		withFilters(action: "save") {
			controller.save()
		}

		then:
		thrown(ValidationException)
	}

	def "save() calls dashboardService.create()"() {
		setup:

		List<DashboardItem> items = new ArrayList<DashboardItem>()
		items.add(new DashboardItem(title: "test1"))
		items.add(new DashboardItem(title: "test2"))

		def dashboard = Mock(Dashboard)
		def dashboardService = Mock(DashboardService)
		controller.dashboardService = dashboardService

		when:
		request.addHeader("Authorization", "Token myApiKey")
		request.JSON = [
				id   : "dashboard",
				name : "new dashboard",
				layout: "{}",
				items: items
		]
		request.requestURI = "/api/v1/dashboards"
		withFilters(action: "save") {
			controller.save()
		}

		then:
		response.status == 200
		1 * dashboardService.create(_, me) >> {
			dashboard
		}
		1 * dashboard.toMap() >> {
			[:]
		}
	}

	def "update() throws ValidationException given incomplete json"() {
		when:
		params.id = 1L
		request.addHeader("Authorization", "Token myApiKey")
		request.JSON = [
				name: "",
		]
		request.requestURI = "/api/v1/dashboards"
		withFilters(action: "update") {
			controller.update()
		}

		then:
		thrown(ValidationException)
	}

	def "update() delegates to dashboardService.update"() {
		setup:
		List<DashboardItem> items = new ArrayList<DashboardItem>()
		DashboardItem item1 = new DashboardItem(title: "test1")
		item1.id = "1"
		DashboardItem item2 = new DashboardItem(title: "test2")
		item2.id = "2"
		items.add(item1)
		items.add(item2)

		def dashboard = Mock(Dashboard)
		def dashboardService = Mock(DashboardService)
		controller.dashboardService = dashboardService

		when:
		params.id = "4"
		request.addHeader("Authorization", "Token myApiKey")
		request.JSON = [
				layout: "{}",
				name : "new dashboard",
				items: items
		]
		request.requestURI = "/api/v1/dashboards"
		withFilters(action: "save") {
			controller.update()
		}

		then:
		response.status == 200
		response.json.a == 1
		1 * dashboardService.update("4", _, me) >> { String id, SaveDashboardCommand command, SecUser user ->
			command.name == "new dashboard"
			return dashboard
		}
		1 * dashboard.toMap() >> [a:1]
	}

	def "delete() delegates to dashboardService.deleteById(String, SecUser)"() {
		when:
		params.id = "3"
		request.addHeader("Authorization", "Token myApiKey")
		request.requestURI = "/api/v1/dashboards/"
		withFilters(action: "delete") {
			controller.delete()
		}

		then:
		response.status == 204
		1 * dashboardService.deleteById("3", me)
		0 * dashboardService._
	}
}
