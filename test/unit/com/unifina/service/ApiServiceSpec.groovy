package com.unifina.service

import com.unifina.api.DashboardListParams
import com.unifina.api.ListParams
import com.unifina.api.NotFoundException
import com.unifina.api.NotPermittedException
import com.unifina.api.ValidationException
import com.unifina.domain.dashboard.Dashboard
import com.unifina.domain.security.Permission
import com.unifina.domain.security.SecUser
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.web.ControllerUnitTestMixin
import spock.lang.Specification

import javax.servlet.http.HttpServletResponse

@TestMixin(ControllerUnitTestMixin) // "as JSON" converter
@TestFor(ApiService)
@Mock(Dashboard)
class ApiServiceSpec extends Specification {

	void "list() returns streams with share permission"() {
		def permissionService = service.permissionService = Mock(PermissionService)
		ListParams listParams = new DashboardListParams(operation: Permission.Operation.SHARE, publicAccess: true)
		SecUser me = new SecUser(username: "me@me.com")

		when:
		def list = service.list(Dashboard, listParams, me)

		then:
		list.size() == 3
		1 * permissionService.get(Dashboard, me, Permission.Operation.SHARE, true, _) >> [
			new Dashboard(), new Dashboard(), new Dashboard()
		]
	}

	void "list() delegates to permissionService#get"() {
		def permissionService = service.permissionService = Mock(PermissionService)

		SecUser me = new SecUser(username: "me@me.com")
		ListParams listParams = new DashboardListParams(publicAccess: true)

		when:
		def list = service.list(Dashboard, listParams, me)

		then:
		list.size() == 3
		1 * permissionService.get(Dashboard, me, Permission.Operation.READ, true, _) >> [
			new Dashboard(), new Dashboard(), new Dashboard()
		]
	}

	void "list() passes user as null to permissionService#get if grantedAccess=false"() {
		def permissionService = service.permissionService = Mock(PermissionService)

		SecUser me = new SecUser(username: "me@me.com")
		ListParams listParams = new DashboardListParams(publicAccess: true, grantedAccess: false)

		when:
		service.list(Dashboard, listParams, me)

		then:
		1 * permissionService.get(Dashboard, null, _, _, _)
	}

	void "list() invokes listParams#validate and listParams#createListCriteria and passes returned closure to permissionService#get"() {
		def permissionService = service.permissionService = Mock(PermissionService)

		SecUser me = new SecUser(username: "me@me.com")
		ListParams listParams = Mock(ListParams)

		when:
		service.list(Dashboard, listParams, me)

		then:
		1 * permissionService.get(_, _, _, _, { it() == "see me?" })
		1 * listParams.validate() >> true
		1 * listParams.createListCriteria() >> { { a -> "see me?" } }
	}

	void "list() throws ValidationException if validation of listParams fails"() {
		SecUser me = new SecUser(username: "me@me.com")
		ListParams listParams = new DashboardListParams(order: null)

		when:
		service.list(Dashboard, listParams, me)
		then:
		thrown(ValidationException)
	}

	void "addLinkHintToHeader() does nothing if offset != max"() {
		def response = Mock(HttpServletResponse)
		when:
		service.addLinkHintToHeader(new DashboardListParams(), 99, [:], response)
		then:
		0 * response._
	}

	void "addLinkHintToHeader() adds link header"() {
		def response = Mock(HttpServletResponse)
		def params = new DashboardListParams(offset: 150, name: "dashboard", publicAccess: true)

		when:
		service.addLinkHintToHeader(params, 1000, [action: "index", controller: "dashboardApi"], response)
		then:
		1 * response.addHeader("Link", '<http://localhost:8080/api/v1/dashboards?max=1000&offset=1150&grantedAccess=true&publicAccess=true&name=dashboard>; rel="more"')
	}

	void "getByIdAndThrowIfNotFound() throws NotFoundException if domain object cannot be found"() {
		when:
		service.getByIdAndThrowIfNotFound(Dashboard, "dashboard-id")

		then:
		def e = thrown(NotFoundException)
		e.asApiError().toMap() == [
			id: "dashboard-id",
			message: "Dashboard with id dashboard-id not found",
			code: "NOT_FOUND",
			fault: "id",
			type: "Dashboard"
		]
	}

	void "getByIdAndThrowIfNotFound() returns domain object if it exists"() {
		Dashboard dashboard = new Dashboard(name: "dashboard")
		dashboard.id = "dashboard-id"
		dashboard.save(failOnError: true)

		expect:
		service.getByIdAndThrowIfNotFound(Dashboard, "dashboard-id") == dashboard
	}

	void "authorizedGetById() throws NotFoundException if domain object cannot be found"() {
		SecUser me = new SecUser(username: "me@me.com")

		when:
		service.authorizedGetById(Dashboard, "dashboard-id", me, Permission.Operation.WRITE)

		then:
		def e = thrown(NotFoundException)
		e.asApiError().toMap() == [
		    id: "dashboard-id",
			message: "Dashboard with id dashboard-id not found",
			code: "NOT_FOUND",
			fault: "id",
			type: "Dashboard"
		]
	}

	void "authorizedGetById() throws NotPermittedException if user does not have required permission"() {
		SecUser me = new SecUser(username: "me@me.com")
		Dashboard dashboard = new Dashboard(name: "dashboard")
		dashboard.id = "dashboard-id"
		dashboard.save(failOnError: true)

		service.permissionService = new PermissionService()

		when:
		service.authorizedGetById(Dashboard, "dashboard-id", me, Permission.Operation.WRITE)

		then:
		def e = thrown(NotPermittedException)
		e.message == "me@me.com does not have permission to write Dashboard (id dashboard-id)"
	}

	void "authorizedGetById() returns domain object if it exists and user has required permission"() {
		SecUser me = new SecUser(username: "me@me.com")
		Dashboard dashboard = new Dashboard(name: "dashboard")
		dashboard.id = "dashboard-id"
		dashboard.save(failOnError: true)

		service.permissionService = Stub(PermissionService) // replace verify() with nop method

		when:
		def result = service.authorizedGetById(Dashboard, "dashboard-id", me, Permission.Operation.WRITE)

		then:
		result == dashboard
	}
}
