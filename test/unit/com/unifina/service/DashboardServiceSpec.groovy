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
			def d = new Dashboard(id: "my-dashboard-$it", name: "my-dashboard-$it", user: user).save(failOnError: true)
			if (it == 3) {
				d.addToItems(new DashboardItem(id: "item-1-$it", title: "item1").save(validate: false, failOnError: true))
				d.addToItems(new DashboardItem(id: "item-2-$it", title: "item2").save(validate: false, failOnError: true))
				d.addToItems(new DashboardItem(id: "item-3-$it", title: "item3").save(validate: false, failOnError: true))
			}
		}

		(1..3).each {
			def d = new Dashboard(id: "not-my-dashboard-$it", name: "not-my-dashboard-$it", user: otherUser).save(failOnError: true)
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
		service.findById("noexistent", user)
		then:
		thrown(NotFoundException)
	}

	def "findById() cannot fetch other user's non-readable dashboard"() {
		when:
		service.findById("not-my-dashboard-1", user)
		then:
		thrown(NotPermittedException)
	}

	def "findById() can fetch readable dashboard"() {
		when:
		def dashboard2 = service.findById("my-dashboard-2", user)
		def dashboard3 = service.findById("my-dashboard-3", user)
		then:
		dashboard2.id == "my-dashboard-2"
		dashboard3.id == "my-dashboard-3"
	}

	def "deleteById() cannot delete non-existent dashboard"() {
		when:
		service.deleteById("nonexistent", user)
		then:
		thrown(NotFoundException)
	}

	def "deleteById() cannot delete other user's non-writeable dashboard"() {
		when:
		service.deleteById("not-my-dashboard-1", user)
		then:
		thrown(NotPermittedException)
	}

	def "deleteById() can delete writable dashboard"() {
		assert Dashboard.findById("my-dashboard-2") != null

		when:
		service.deleteById("my-dashboard-2", user)
		then:
		Dashboard.findById("my-dashboard-2") == null
	}

	def "create() creates a new dashboard and returns it"() {
		setup:
		def user = new SecUser(name: "tester").save(validate: false)
		when:
		SaveDashboardCommand command = new SaveDashboardCommand([
				id   : "1",
				name : "test-create",
				items: [
						new SaveDashboardItemCommand(id: "1", title: "test1", canvas: new Canvas(), module: 0, webcomponent: "b"),
						new SaveDashboardItemCommand(id: "2", title: "test2", canvas: new Canvas(), module: 0, webcomponent: "b")
				]
		])
		service.create(command, user)

		then:
		// 6 created in setup and this one
		Dashboard.count() == 7
		Dashboard.findByName("test-create").getName() == "test-create"
		Dashboard.findByName("test-create").getItems().first().title == "test1"
		Dashboard.findByName("test-create").getItems().last().title == "test2"
		Dashboard.findByName("test-create").getUser().getName() == "tester"
	}

	def "update() cannot update non-existent dashboard"() {
		when:
		service.update(new SaveDashboardCommand(id: "nonexistent", name: "newName"), user)
		then:
		thrown(NotFoundException)
	}

	def "update() cannot update other user's non-writeable dashboard"() {
		when:
		service.update(new SaveDashboardCommand(id: "not-my-dashboard-1", name: "newName"), user)
		then:
		thrown(NotPermittedException)
	}

	def "update() can update writable dashboard"() {
		when:
		def dashboard = service.update(new SaveDashboardCommand(id: "my-dashboard-2", name: "newName"), user)
		then:
		dashboard != null
		dashboard.name == "newName"
		Dashboard.findById("my-dashboard-2").name == "newName"
	}

	def "findDashboardItem() cannot fetch non-existent dashboard"() {
		when:
		service.findDashboardItem("nonexistent", "nonexistent", user)
		then:
		thrown(NotFoundException)
	}

	def "findDashboardItem() cannot fetch non-existent dashboard item"() {
		when:
		service.findDashboardItem("my-dashboard-1", "item-1-2", user)
		then:
		thrown(NotFoundException)
	}

	def "findDashboardItem() cannot fetch other user's non-readable dashboard"() {
		when:
		service.findDashboardItem("not-my-dashboard-1", "nonexistent", user)
		then:
		thrown(NotPermittedException)
	}

	def "findDashboardItem() can fetch existing dashboard item from readable dashboard"() {
		when:
		def item = service.findDashboardItem("my-dashboard-3", "item-1-3", user)
		then:
		item != null
		item.id == "item-1-3"
	}

	def "addDashboardItem() cannot add item to non-existent dashboard"() {
		def command = new SaveDashboardItemCommand(
				id: "test",
				title: "added-item",
				canvas: new Canvas(),
				module: 1
		)

		when:
		service.addDashboardItem("nonexistent", command, user)
		then:
		thrown(NotFoundException)
	}

	def "addDashboardItem() cannot add item other user's non-writeable dashboard"() {
		def command = new SaveDashboardItemCommand(
				id: "test",
				title: "added-item",
				canvas: new Canvas(),
				module: 1
		)

		when:
		service.addDashboardItem("not-my-dashboard-1", command, user)
		then:
		thrown(NotPermittedException)
	}

	def "addDashboardItem() cannot add with item non-valid command object"() {
		when:
		service.addDashboardItem("my-dashboard-1", new SaveDashboardItemCommand(), user)
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
				id: "test",
				title: "added-item",
				canvas: canvas,
				module: 1
		)

		when:
		def item = service.addDashboardItem("my-dashboard-2", command, user)

		then:
		item instanceof DashboardItem
		item.id == "test"
		item.title == "added-item"
		item.canvas.id == "1"
		item.module == 1
		item.webcomponent == "streamr-chart"

		and:
		Dashboard.get("my-dashboard-2").items*.id == [item.id]
	}


	def "updateDashboardItem() cannot update item from non-existent dashboard"() {
		setup:
		def json = new JsonBuilder([
				modules: [
						[hash: 1, uiChannel: [webcomponent: "streamr-chart"]]
				]
		]).toString()

		def canvas = new Canvas(json: json).save(failOnError: true, validate: false)

		def command = new SaveDashboardItemCommand(
				id: "test",
				title: "added-item",
				canvas: canvas,
				module: 1
		)

		when:
		service.updateDashboardItem("nonexistent", "test", command, user)
		then:
		thrown(NotFoundException)
	}

	def "updateDashboardItem() cannot update item from other user's non-writeable dashboard"() {
		setup:
		def json = new JsonBuilder([
				modules: [
						[hash: 1, uiChannel: [webcomponent: "streamr-chart"]]
				]
		]).toString()

		def canvas = new Canvas(json: json).save(failOnError: true, validate: false)

		def command = new SaveDashboardItemCommand(
				id: "test",
				title: "added-item",
				canvas: canvas,
				module: 1
		)

		when:
		service.updateDashboardItem("not-my-dashboard-2", "test", command, user)
		then:
		thrown(NotPermittedException)
	}

	def "updateDashboardItem() cannot update non-existent dashboard - dashboard item -pair"() {
		setup:
		def json = new JsonBuilder([
				modules: [
						[hash: 1, uiChannel: [webcomponent: "streamr-chart"]]
				]
		]).toString()

		def canvas = new Canvas(json: json).save(failOnError: true, validate: false)

		def command = new SaveDashboardItemCommand(
				id: "item-1-3",
				title: "added-item",
				canvas: canvas,
				module: 1
		)

		when:
		service.updateDashboardItem("my-dashboard-2", "item-1-3", command, user)
		then:
		thrown(NotFoundException)
	}

	def "updateDashboardItem() throws ValidationException given non valid command object"() {
		when:
		service.updateDashboardItem("my-dashboard-1", "test", new SaveDashboardItemCommand(), user)
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

		def dashboardId = "my-dashboard-3"
		def itemToUpdateId = "item-1-3"

		def command = new SaveDashboardItemCommand(
				id: itemToUpdateId,
				title: "updated-item",
				canvas: canvas,
				module: 1
		)

		assert DashboardItem.findById(itemToUpdateId) != null

		when:
		def updatedItem = service.updateDashboardItem(dashboardId, itemToUpdateId, command, user)
		then:
		DashboardItem.findById(itemToUpdateId).properties == updatedItem.properties
		updatedItem.toMap() == [
				id          : itemToUpdateId,
				dashboard   : dashboardId,
				title       : "updated-item",
				canvas      : canvas.id,
				module      : 1,
				webcomponent: "streamr-chart"
		]
	}

	def "deleteDashboardItem() cannot delete item from non-existent dashboard"() {
		when:
		service.deleteDashboardItem("nonexistent", "nonexistent", user)
		then:
		thrown(NotFoundException)
	}

	def "deleteDashboardItem() cannot delete item from other user's non-writable dashboard"() {
		when:
		service.deleteDashboardItem("not-my-dashboard-1", "nonexistent", user)
		then:
		thrown(NotPermittedException)
	}

	def "deleteDashboardItem() cannot delete non-existent dashboard item"() {
		when:
		service.deleteDashboardItem("my-dashboard-2", "nonexistent", user)
		then:
		thrown(NotFoundException)
	}

	def "deleteDashboardItem() can remove item from dashboard and delete it"() {
		def dashboardId = "my-dashboard-3"
		def itemToRemoveId = "item-1-3"

		assert DashboardItem.findById(itemToRemoveId) != null
		assert Dashboard.get(dashboardId).items*.id == ["item-1-3", "item-2-3", "item-3-3"]

		when:
		service.deleteDashboardItem(dashboardId, itemToRemoveId, user)
		then:
		DashboardItem.findById(itemToRemoveId) == null
		Dashboard.get(dashboardId).items*.id == ["item-2-3", "item-3-3"]
	}
}
