package com.unifina.service

import com.unifina.api.NotFoundException
import com.unifina.api.NotPermittedException
import com.unifina.api.SaveDashboardCommand
import com.unifina.domain.dashboard.Dashboard
import com.unifina.domain.security.Permission
import com.unifina.domain.security.SecUser
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import spock.lang.Specification

@TestFor(DashboardService)
@Mock([Dashboard, Permission, SecUser])
class DashboardServiceSpec extends Specification {

	SecUser user = new SecUser(username: "e@e.com", name: "user")
	SecUser otherUser = new SecUser(username: "a@a.com", name: "someoneElse")

	def setup() {
		PermissionService permissionService = service.permissionService = new PermissionService()
		permissionService.grailsApplication = grailsApplication


		user.save(failOnError: true, validate: false)
		otherUser.save(failOnError: true, validate: false)

		(1..3).each {
			new Dashboard(name: "my-dashboard-$it", user: user).save(failOnError: true)
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
}
