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

	SecUser user = new SecUser(username: "first@user.com", name: "user")
	SecUser otherUser = new SecUser(username: "second@user.com", name: "someoneElse")

	def setup() {
		PermissionService permissionService = service.permissionService = new PermissionService()
		permissionService.grailsApplication = grailsApplication

		user.save(failOnError: true, validate: false)
		otherUser.save(failOnError: true, validate: false)

		(1..3).each {
			def d = new Dashboard(name: "my-dashboard-$it", user: user).save(failOnError: true)
			if (it == 3) {
				d.addToItems(new DashboardItem(title: "item1").save(validate: false, failOnError: true))
				d.addToItems(new DashboardItem(title: "item2").save(validate: false, failOnError: true))
				d.addToItems(new DashboardItem(title: "item3").save(validate: false, failOnError: true))
			}
		}

		(1..3).each {
			def d = new Dashboard(name: "not-my-dashboard-$it", user: otherUser).save(failOnError: true)
			if (it == 2) {
				permissionService.grant(otherUser, d, user, Permission.Operation.READ)
			}
		}
	}

	def "get all dashboards of user"() {
		when:
		def dashboards = service.findAllDashboards(user)
		then:
		dashboards*.name as Set == ["my-dashboard-1", "my-dashboard-2", "my-dashboard-3", "not-my-dashboard-2"] as Set
	}

	def "findById() cannot fetch non-existent dashboard"() {
		when:
		service.findById("noexistent", user)
		then:
		thrown(NotFoundException)
	}

	def "findById() cannot fetch other user's non-readable dashboard"() {
		when:
		service.findById("4", user)
		then:
		thrown(NotPermittedException)
	}

	def "findById() can fetch readable dashboard"() {
		when:
		def dashboard2 = service.findById("2", user)
		def dashboard3 = service.findById("5", user)
		then:
		dashboard2.id == "2"
		dashboard3.id == "5"
	}

	def "deleteById() cannot delete non-existent dashboard"() {
		when:
		service.deleteById("nonexistent", user)
		then:
		thrown(NotFoundException)
	}

	def "deleteById() cannot delete other user's non-writeable dashboard"() {
		when:
		service.deleteById("4", user)
		then:
		thrown(NotPermittedException)
	}

	def "deleteById() can delete writable dashboard"() {
		assert Dashboard.findById("2") != null

		when:
		service.deleteById("2", user)
		then:
		Dashboard.findById("2") == null
	}

	def "create() creates a new dashboard and returns it"() {
		setup:
		def user = new SecUser(name: "tester").save(validate: false)
		def canvas = new Canvas(json: new JsonBuilder([
				modules: [
						[hash: 1, uiChannel: [webcomponent: "streamr-chart"]]
				]
		]).toString()).save(failOnError: true, validate: false)
		when:
		SaveDashboardCommand command = new SaveDashboardCommand([
				name : "test-create",
				items: [
						new SaveDashboardItemCommand(title: "test1", canvas: canvas, module: 1),
						new SaveDashboardItemCommand(title: "test2", canvas: canvas, module: 1)
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
		service.update("nonexistent", new SaveDashboardCommand(name: "newName"), user)
		then:
		thrown(NotFoundException)
	}

	def "update() cannot update other user's non-writeable dashboard"() {
		when:
		service.update("4", new SaveDashboardCommand(name: "newName"), user)
		then:
		thrown(NotPermittedException)
	}

	def "update() can update writable dashboard"() {
		when:
		def dashboard = service.update("2", new SaveDashboardCommand(name: "newName"), user)
		then:
		dashboard != null
		dashboard.name == "newName"
		Dashboard.findById("2").name == "newName"
	}

	def "findDashboardItem() cannot fetch non-existent dashboard"() {
		when:
		service.findDashboardItem("nonexistent", "nonexistent", user)
		then:
		thrown(NotFoundException)
	}

	def "findDashboardItem() cannot fetch non-existent dashboard item"() {
		when:
		service.findDashboardItem("1", "item-1-2", user)
		then:
		thrown(NotFoundException)
	}

	def "findDashboardItem() cannot fetch other user's non-readable dashboard"() {
		when:
		service.findDashboardItem("4", "nonexistent", user)
		then:
		thrown(NotPermittedException)
	}

	def "findDashboardItem() can fetch existing dashboard item from readable dashboard"() {
		when:
		def item = service.findDashboardItem("3", "1", user)
		then:
		item != null
		item.id == "1"
	}

	def "addDashboardItem() cannot add item to non-existent dashboard"() {
		def command = new SaveDashboardItemCommand(
				title: "added-item",
				canvas: new Canvas(json: new JsonBuilder([
					modules: [
						[hash: 1, uiChannel: [webcomponent: "streamr-chart"]]
					]
				]).toString()).save(failOnError: true, validate: false),
				module: 1,
				webcomponent: "streamr-chart"
		)

		when:
		service.addDashboardItem("nonexistent", command, user)
		then:
		thrown(NotFoundException)
	}

	def "addDashboardItem() cannot add item other user's non-writeable dashboard"() {
		def command = new SaveDashboardItemCommand(
				title: "added-item",
				canvas: new Canvas(json: new JsonBuilder([
					modules: [
							[hash: 1, uiChannel: [webcomponent: "streamr-chart"]]
					]
				]).toString()).save(failOnError: true, validate: false),
				module: 1,
				webcomponent: "streamr-chart"
		)

		when:
		service.addDashboardItem("4", command, user)
		then:
		thrown(NotPermittedException)
	}

	def "addDashboardItem() cannot add with item non-valid command object"() {
		when:
		service.addDashboardItem("1", new SaveDashboardItemCommand(), user)
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
				canvas: canvas,
				module: 1,
				webcomponent: "streamr-chart"
		)

		when:
		def item = service.addDashboardItem("2", command, user)

		then:
		item instanceof DashboardItem
		item.id == "4"
		item.title == "added-item"
		item.canvas.id == "1"
		item.module == 1
		item.webcomponent.getName() == "streamr-chart"

		and:
		Dashboard.get("2").items*.id == [item.id]
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
				title: "added-item",
				canvas: canvas,
				module: 1,
				webcomponent: "streamr-chart"
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
				title: "added-item",
				canvas: canvas,
				module: 1,
				webcomponent: "streamr-chart"
		)

		when:
		service.updateDashboardItem("5", "test", command, user)
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
				title: "added-item",
				canvas: canvas,
				module: 1,
				webcomponent: "streamr-chart"
		)

		when:
		service.updateDashboardItem("2", "1", command, user)
		then:
		thrown(NotFoundException)
	}

	def "updateDashboardItem() throws ValidationException given non valid command object"() {
		when:
		service.updateDashboardItem("1", "test", new SaveDashboardItemCommand(), user)
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

		def dashboardId = "3"
		def itemToUpdateId = "1"

		def command = new SaveDashboardItemCommand(
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
		service.deleteDashboardItem("4", "nonexistent", user)
		then:
		thrown(NotPermittedException)
	}

	def "deleteDashboardItem() cannot delete non-existent dashboard item"() {
		when:
		service.deleteDashboardItem("2", "nonexistent", user)
		then:
		thrown(NotFoundException)
	}

	def "deleteDashboardItem() can remove item from dashboard and delete it"() {
		def dashboardId = "3"
		def itemToRemoveId = "1"

		assert DashboardItem.findById(itemToRemoveId) != null
		assert Dashboard.get(dashboardId).items*.id == ["1", "2", "3"]

		when:
		service.deleteDashboardItem(dashboardId, itemToRemoveId, user)
		then:
		DashboardItem.findById(itemToRemoveId) == null
		Dashboard.get(dashboardId).items*.id == ["2", "3"]
	}
}
