package com.unifina.service

import com.unifina.api.*
import com.unifina.domain.dashboard.Dashboard
import com.unifina.domain.dashboard.DashboardItem
import com.unifina.domain.security.Permission
import com.unifina.domain.security.SecUser
import com.unifina.domain.signalpath.Canvas
import grails.converters.JSON
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import groovy.json.JsonBuilder
import spock.lang.Specification

@TestFor(DashboardService)
@Mock([Canvas, Dashboard, DashboardItem, Permission, SecUser])
class DashboardServiceSpec extends Specification {

	SecUser user = new SecUser(username: "e@e.com", name: "user")
	SecUser otherUser = new SecUser(username: "a@a.com", name: "someoneElse")

	def setup() {
		PermissionService permissionService = service.permissionService = new PermissionService()
		permissionService.grailsApplication = grailsApplication


		user.save(failOnError: true, validate: false)
		otherUser.save(failOnError: true, validate: false)

		(1..3).each {
			def d = new Dashboard(name: "my-dashboard-$it", user: user).save(failOnError: true)
			if (it == 3) {
				d.addToItems(new DashboardItem(title: "item1", ord: 1, size: "large").save(validate: false, failOnError: true))
				d.addToItems(new DashboardItem(title: "item2", ord: 0, size: "medium").save(validate: false, failOnError: true))
				d.addToItems(new DashboardItem(title: "item3", ord: 2, size: "large").save(validate: false, failOnError: true))
			}
		}

		(1..3).each {
			def d = new Dashboard(name: "not-my-dashboard-$it", user: otherUser).save(failOnError: true)
			if (it == 2) {
				println("Object is " + d)
				permissionService.grant(otherUser, d, user, Permission.Operation.READ)
			}
		}
	}

	def "get all dashboards of user"() {
		when:
		def dashboards = service.findAllDashboards(user)
		then:
		dashboards*.name == ["my-dashboard-1", "my-dashboard-2", "my-dashboard-3", "not-my-dashboard-2"]
	}

	def "findById() cannot fetch non-existent dashboard"() {
		when:
		service.findById(666L, user)
		then:
		thrown(NotFoundException)
	}

	def "findById() cannot fetch other user's non-readable dashboard"() {
		when:
		service.findById(4L, user)
		then:
		thrown(NotPermittedException)
	}

	def "findById() can fetch readable dashboard"() {
		when:
		def dashboard2 = service.findById(2L, user)
		def dashboard5 = service.findById(5L, user)
		then:
		dashboard2.id == 2L
		dashboard5.id == 5L
	}

	def "deleteById() cannot delete non-existent dashboard"() {
		when:
		service.deleteById(666L, user)
		then:
		thrown(NotFoundException)
	}

	def "deleteById() cannot delete other user's non-writeable dashboard"() {
		when:
		service.deleteById(5L, user)
		then:
		thrown(NotPermittedException)
	}

	def "deleteById() can delete writable dashboard"() {
		assert Dashboard.findById(2L) != null

		when:
		service.deleteById(2L, user)
		then:
		Dashboard.findById(2L) == null
	}

	def "create() creates a new dashboard and returns it"() {
		setup:
		SortedSet<DashboardItem> items = new TreeSet<DashboardItem>()
		items.add(new DashboardItem(title: "test1", ord: new Integer(0), canvas: new Canvas(), module: 0, size: "b", webcomponent: "b"))
		items.add(new DashboardItem(title: "test2", ord: new Integer(1), canvas: new Canvas(), module: 0, size: "b", webcomponent: "b"))
		def user = new SecUser(name: "tester").save(validate: false)
		when:
		SaveDashboardCommand command = new SaveDashboardCommand([
		        name: "test-create",
				items: items
		])
		service.create(command, user)

		then:
		// 6 created in setup and this one
		Dashboard.count() == 7
		Dashboard.findByName("test-create").getName() == "test-create"
		Dashboard.findByName("test-create").getItems().first().title == "test1"
		Dashboard.findByName("test-create").getItems().last().title == "test2"
		Dashboard.findByName("test-create").getUser().getName() =="tester"

	}

	def "update() cannot update non-existent dashboard"() {
		when:
		service.update(666L, new SaveDashboardCommand(), user)
		then:
		thrown(NotFoundException)
	}

	def "update() cannot update other user's non-writeable dashboard"() {
		when:
		service.update(5L, new SaveDashboardCommand(), user)
		then:
		thrown(NotPermittedException)
	}

	def "update() can update writable dashboard"() {
		when:
		def dashboard = service.update(2L, new SaveDashboardCommand(name: "newName"), user)
		then:
		dashboard != null
		dashboard.name == "newName"
		Dashboard.findById(2L).name == "newName"
	}

	def "findDashboardItem() cannot fetch non-existent dashboard"() {
		when:
		service.findDashboardItem(666L, 1L, user)
		then:
		thrown(NotFoundException)
	}

	def "findDashboardItem() cannot fetch non-existent dashboard item"() {
		when:
		service.findDashboardItem(3L, 4L, user)
		then:
		thrown(NotFoundException)
	}

	def "findDashboardItem() cannot fetch other user's non-readable dashboard"() {
		when:
		service.findDashboardItem(4L, 666L, user)
		then:
		thrown(NotPermittedException)
	}

	def "findDashboardItem() can fetch existing dashboard item from readable dashboard"() {
		when:
		def item = service.findDashboardItem(3L, 1L, user)
		then:
		item.id == 1L
	}

	def "addDashboardItem() cannot add item to non-existent dashboard"() {
		def canvas = new Canvas().save(failOnError: true, validate: false)

		def command = new SaveDashboardItemCommand(
			title: "added-item",
			canvas: canvas.id,
			module: 1,
			ord: 666,
			size: "large"
		)

		when:
		service.addDashboardItem(666L, command, user)
		then:
		thrown(NotFoundException)
	}

	def "addDashboardItem() cannot add item other user's non-writeable dashboard"() {
		def canvas = new Canvas().save(failOnError: true, validate: false)

		def command = new SaveDashboardItemCommand(
			title: "added-item",
			canvas: canvas.id,
			module: 1,
			ord: 666,
			size: "large"
		)

		when:
		service.addDashboardItem(5L, command, user)
		then:
		thrown(NotPermittedException)
	}

	def "addDashboardItem() cannot add with item non-valid command object"() {
		when:
		service.addDashboardItem(2L, new SaveDashboardItemCommand(), user)
		then:
		thrown(ValidationException)
	}

	def "addDashboardItem() adds item to existing dashboard"() {
		setup:
		def json = new JsonBuilder([
		    modules: [
		        [hash: 1, uiChannel: [webcomponent: "streamr-chart"]]
		    ]
		]).toString()

		def canvas = new Canvas(json: json).save(failOnError: true, validate: false)

		def command = new SaveDashboardItemCommand(
			title: "added-item",
			canvas: canvas.id,
			module: 1,
			ord: 666,
			size: "large"
		)

		when:
		def item = service.addDashboardItem(2L, command, user)

		then:
		item instanceof DashboardItem
		item.id != null
		item.title == "added-item"
		item.canvas.id == "1"
		item.module == 1
		item.webcomponent == "streamr-chart"
		item.ord == 666
		item.size == "large"

		and:
		Dashboard.get(2L).items*.id == [item.id]
	}




	def "updateDashboardItem() cannot update item from non-existent dashboard"() {
		def json = new JsonBuilder([
			modules: [
				[hash: 1, uiChannel: [webcomponent: "streamr-chart"]]
			]
		]).toString()

		def canvas = new Canvas(json: json).save(failOnError: true, validate: false)

		def command = new SaveDashboardItemCommand(
			title: "updated-item",
			canvas: canvas.id,
			module: 1,
			ord: 42,
			size: "small"
		)

		when:
		service.updateDashboardItem(666L, 0L, command, user)
		then:
		thrown(NotFoundException)
	}

	def "updateDashboardItem() cannot update item from other user's non-writeable dashboard"() {
		def json = new JsonBuilder([
			modules: [
				[hash: 1, uiChannel: [webcomponent: "streamr-chart"]]
			]
		]).toString()

		def canvas = new Canvas(json: json).save(failOnError: true, validate: false)

		def command = new SaveDashboardItemCommand(
			title: "updated-item",
			canvas: canvas.id,
			module: 1,
			ord: 42,
			size: "small"
		)

		when:
		service.updateDashboardItem(5L, 0L, command, user)
		then:
		thrown(NotPermittedException)
	}

	def "updateDashboardItem() cannot update non-existent dashboard - dashboard item -pair"() {
		def json = new JsonBuilder([
			modules: [
				[hash: 1, uiChannel: [webcomponent: "streamr-chart"]]
			]
		]).toString()

		def canvas = new Canvas(json: json).save(failOnError: true, validate: false)

		def command = new SaveDashboardItemCommand(
			title: "updated-item",
			canvas: canvas.id,
			module: 1,
			ord: 42,
			size: "small"
		)

		when:
		service.updateDashboardItem(2L, 1L, command, user)
		then:
		thrown(NotFoundException)
	}

	def "updateDashboardItem() throws ValidationException given non valid command object"() {
		when:
		service.updateDashboardItem(3L, 1L, new SaveDashboardItemCommand(), user)
		then:
		thrown(ValidationException)
	}

	def "updateDashboardItem() can update item on dashboard."() {
		def json = new JsonBuilder([
			modules: [
				[hash: 1, uiChannel: [webcomponent: "streamr-chart"]]
			]
		]).toString()

		def canvas = new Canvas(json: json).save(failOnError: true, validate: false)

		def command = new SaveDashboardItemCommand(
			title: "updated-item",
			canvas: canvas.id,
			module: 1,
			ord: 42,
			size: "small"
		)

		def dashboardId = 3L
		def itemToUpdateId = 1L

		assert DashboardItem.findById(itemToUpdateId) != null

		when:
		def updatedItem = service.updateDashboardItem(dashboardId, itemToUpdateId, command, user)
		then:
		DashboardItem.findById(itemToUpdateId).properties == updatedItem.properties
		updatedItem.toMap() == [
			id: 1L,
			dashboard: dashboardId,
		    title: "updated-item",
			canvas: canvas.id,
			module: 1,
			webcomponent: "streamr-chart",
			ord: 42,
			size: "small",
		]
	}

	def "deleteDashboardItem() cannot delete item from non-existent dashboard"() {
		when:
		service.deleteDashboardItem(666L, 0L, user)
		then:
		thrown(NotFoundException)
	}

	def "deleteDashboardItem() cannot delete item from other user's non-writeable dashboard"() {
		when:
		service.deleteDashboardItem(5L, 0L, user)
		then:
		thrown(NotPermittedException)
	}

	def "deleteDashboardItem() can delete non-existent dashboard item"() {
		when:
		service.deleteDashboardItem(2L, 1L, user)
		then:
		thrown(NotFoundException)
	}

	def "deleteDashboardItem() can remove item from dashboard and delete it"() {
		def dashboardId = 3L
		def itemToRemoveId = 2L

		assert DashboardItem.findById(itemToRemoveId) != null
		assert Dashboard.get(dashboardId).items*.id == [2L, 1L, 3L]

		when:
		service.deleteDashboardItem(dashboardId, itemToRemoveId, user)
		then:
		DashboardItem.findById(itemToRemoveId) == null
		Dashboard.get(dashboardId).items*.id == [1L, 3L]
	}
}
