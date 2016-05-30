package com.unifina.controller.api

import com.unifina.api.SaveDashboardCommand
import com.unifina.api.ValidationException
import com.unifina.domain.dashboard.Dashboard
import com.unifina.domain.dashboard.DashboardItem
import com.unifina.domain.security.Permission
import com.unifina.domain.security.SecUser
import com.unifina.domain.signalpath.Canvas
import com.unifina.filters.UnifinaCoreAPIFilters
import com.unifina.service.ApiService
import com.unifina.service.DashboardService
import com.unifina.service.PermissionService
import com.unifina.service.UserService
import grails.orm.HibernateCriteriaBuilder
import grails.plugin.springsecurity.SpringSecurityService
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import grails.test.mixin.web.FiltersUnitTestMixin
import spock.lang.Specification

@TestFor(DashboardApiController)
@Mixin(FiltersUnitTestMixin)
@Mock([Canvas, Dashboard, DashboardItem, SecUser, UnifinaCoreAPIFilters, SpringSecurityService, UserService, ApiService])
class DashboardApiControllerSpec extends Specification {

	DashboardService dashboardService
	SecUser me
	List<Dashboard> dashboards

	def setup() {
		dashboardService = controller.dashboardService = Mock(DashboardService)
		controller.permissionService = Mock(PermissionService)
		controller.apiService = mainContext.getBean(ApiService)

		me = new SecUser(apiKey: "myApiKey").save(failOnError: true, validate: false)
		dashboards = initDashboards(me)
	}

	public static List<Dashboard> initDashboards(SecUser me) {
		List<Dashboard> dashboards = []
		dashboards.add(new Dashboard(name: "dashboard-1", user: me))
		dashboards.add(new Dashboard(name: "dashboard-2", user: me))
		dashboards.add(new Dashboard(name: "dashboard-3", user: me))

		Canvas canvas = new Canvas()
		canvas.save(failOnError: true, validate: false)

		dashboards[1].addToItems(new DashboardItem(
			title: "dashboard-2-item",
			ord: 0,
			size: "large",
			canvas: canvas,
			module: 1,
			webcomponent: "streamr-component",
			dashboard: dashboards[1]
		).save(failOnError: true))

		dashboards[2].addToItems(new DashboardItem(
			title: "dashboard-3-item-1",
			ord: 1,
			size: "small",
			canvas: canvas,
			module: 2,
			webcomponent: "streamr-table",
			dashboard: dashboards[2]
		).save(failOnError: true))
		dashboards[2].addToItems(new DashboardItem(
			title: "dashboard-3-item-2",
			ord: 0,
			size: "x-large",
			canvas: canvas,
			module: 3,
			webcomponent: "streamr-chart",
			dashboard: dashboards[2]
		).save(failOnError: true))

		dashboards*.save(failOnError: true)
		return dashboards
	}

	void "index() renders authorized dashboards as a list"() {
		when:
		request.addHeader("Authorization", "Token $me.apiKey")
		request.requestURI = "/api/v1/dashboards"
		withFilters(action: "index") {
			controller.index()
		}

		then:
		response.status == 200
		response.json.size() == dashboards.size()
		1 * controller.permissionService.get(Dashboard, me, Permission.Operation.READ, false, _) >> dashboards
	}

	void "index() adds name param to filter criteria"() {
		def criteriaBuilderMock = Mock(HibernateCriteriaBuilder)

		when:
		params.name = "Foo"
		request.addHeader("Authorization", "Token $me.apiKey")
		request.requestURI = "/api/v1/dashboards"
		withFilters(action: "index") {
			controller.index()
		}

		then:
		1 * controller.permissionService.get(Dashboard, me, Permission.Operation.READ, false, _) >> { Class resource, SecUser u, Permission.Operation op, boolean pub, Closure criteria ->
			criteria.delegate = criteriaBuilderMock
			criteria()
			return []
		}
		and:
		1 * criteriaBuilderMock.eq("name", "Foo")
	}

	def "show() shows dashboard with 0 items"() {
		when:
		params.id = 1L
		request.addHeader("Authorization", "Token $me.apiKey")
		request.requestURI = "/api/v1/dashboards"
		withFilters(action: "show") {
			controller.show()
		}

		then:
		response.status == 200
		response.json == [
			id: 1,
			name: "dashboard-1",
			items: [],
		]
		1 * dashboardService.findById(1L, me) >> dashboards[0]
		0 * dashboardService._
	}

	def "show() shows dashboard with many items"() {
		when:
		params.id = 3L
		request.addHeader("Authorization", "Token $me.apiKey")
		request.requestURI = "/api/v1/dashboards"
		withFilters(action: "show") {
			controller.show()
		}

		then:
		response.status == 200
		response.json == [
			id: 3,
			name: "dashboard-3",
			items: [
			    [
					id: 3,
					dashboard: dashboards[2].id,
					ord: 0,
			        title: "dashboard-3-item-2",
					size: "x-large",
					canvas: "1",
					module: 3,
					webcomponent: "streamr-chart"
			    ],
				[
					id: 2,
					dashboard: dashboards[2].id,
					ord: 1,
					title: "dashboard-3-item-1",
					size: "small",
					canvas: "1",
					module: 2,
					webcomponent: "streamr-table"
				],
			]
		]
		1 * dashboardService.findById(3L, me) >> dashboards[2]
		0 * dashboardService._
	}

	def "save() throws ValidationException given incomplete json"() {
		when:
		request.addHeader("Authorization", "Token $me.apiKey")
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

	def "save() creates dashboard"() {

		Long nextId = dashboards.last().id + 1

		when:
		request.addHeader("Authorization", "Token $me.apiKey")
		request.JSON = [
			name: "new dashboard",
		]
		request.requestURI = "/api/v1/dashboards"
		withFilters(action: "save") {
			controller.save()
		}

		then:
		response.status == 200
		response.json == [
			id: nextId,
			name: "new dashboard",
			items: []
		]
		Dashboard.findById(nextId) != null
	}

	def "update() throws ValidationException given incomplete json"() {
		when:
		params.id = 1L
		request.addHeader("Authorization", "Token $me.apiKey")
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

	def "update() delegates to dashboardService.update and returns new dashboard as result"() {
		when:
		params.id = 3L
		request.addHeader("Authorization", "Token $me.apiKey")
		request.JSON = [
			name: "dashboard-update-3",
		]
		request.requestURI = "/api/v1/dashboards"
		withFilters(action: "update") {
			controller.update()
		}

		then:
		response.status == 200
		response.json == [
			id: 3,
			name: "dashboard-update-3",
			items: [
				[
					id: 3,
					dashboard: dashboards[2].id,
					ord: 0,
					title: "dashboard-3-item-2",
					size: "x-large",
					canvas: "1",
					module: 3,
					webcomponent: "streamr-chart"
				],
				[
					id: 2,
					dashboard: dashboards[2].id,
					ord: 1,
					title: "dashboard-3-item-1",
					size: "small",
					canvas: "1",
					module: 2,
					webcomponent: "streamr-table"
				],
			]
		]
		1 * dashboardService.update(3L, _, me) >> { Long id, SaveDashboardCommand command, SecUser user ->
			def d = dashboards[2]
			d.name = command.name
			return d
		}
		0 * dashboardService._
	}

	def "delete() delegates to dashboardService.deleteById(Long, SecUser)"() {
		when:
		params.id = 3L
		request.addHeader("Authorization", "Token $me.apiKey")
		request.requestURI = "/api/v1/dashboards/"
		withFilters(action: "delete") {
			controller.delete()
		}

		then:
		response.status == 204
		1 * dashboardService.deleteById(3, me)
		0 * dashboardService._
	}
}
