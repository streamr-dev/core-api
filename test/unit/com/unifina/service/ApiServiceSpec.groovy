package com.unifina.service


import com.unifina.domain.Dashboard
import com.unifina.domain.Permission
import com.unifina.domain.User
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.web.ControllerUnitTestMixin
import grails.util.Holders
import spock.lang.Specification

import javax.servlet.http.HttpServletResponse

@TestMixin(ControllerUnitTestMixin) // "as JSON" converter
@TestFor(ApiService)
@Mock(Dashboard)
class ApiServiceSpec extends Specification {

	void "list() returns streams with share permission"() {
		def permissionService = service.permissionService = Mock(PermissionService)
		ListParams listParams = new DashboardListParams(operation: Permission.Operation.DASHBOARD_SHARE, publicAccess: true)
		User me = new User(username: "me@me.com")

		when:
		def list = service.list(Dashboard, listParams, me)

		then:
		list.size() == 3
		1 * permissionService.get(Dashboard, me, Permission.Operation.DASHBOARD_SHARE, true, _) >> [
			new Dashboard(), new Dashboard(), new Dashboard()
		]
	}

	void "list() delegates to permissionService#get"() {
		def permissionService = service.permissionService = Mock(PermissionService)

		User me = new User(username: "me@me.com")
		ListParams listParams = new DashboardListParams(publicAccess: true)

		when:
		def list = service.list(Dashboard, listParams, me)

		then:
		list.size() == 3
		1 * permissionService.get(Dashboard, me, Permission.Operation.DASHBOARD_GET, true, _) >> [
			new Dashboard(), new Dashboard(), new Dashboard()
		]
	}

	void "list() passes user as null to permissionService#get if grantedAccess=false"() {
		def permissionService = service.permissionService = Mock(PermissionService)

		User me = new User(username: "me@me.com")
		ListParams listParams = new DashboardListParams(publicAccess: true, grantedAccess: false)

		when:
		service.list(Dashboard, listParams, me)

		then:
		1 * permissionService.get(Dashboard, null, _, _, _)
	}

	void "list() invokes listParams#validate and listParams#createListCriteria and passes returned closure to permissionService#get"() {
		def permissionService = service.permissionService = Mock(PermissionService)

		User me = new User(username: "me@me.com")
		ListParams listParams = Mock(ListParams)

		when:
		service.list(Dashboard, listParams, me)

		then:
		1 * permissionService.get(_, _, _, _, { it() == "see me?" })
		1 * listParams.validate() >> true
		1 * listParams.createListCriteria() >> { { a -> "see me?" } }
	}

	void "list() throws ValidationException if validation of listParams fails"() {
		User me = new User(username: "me@me.com")
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
		1 * response.addHeader("Link", '<'+ Holders.grailsApplication.config.grails.serverURL+'/api/v1/dashboards?max=1000&offset=1150&grantedAccess=true&publicAccess=true&name=dashboard>; rel="more"')
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
		User me = new User(username: "me@me.com")

		when:
		service.authorizedGetById(Dashboard, "dashboard-id", me, Permission.Operation.DASHBOARD_EDIT)

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
		User me = new User(username: "me@me.com")
		Dashboard dashboard = new Dashboard(name: "dashboard")
		dashboard.id = "dashboard-id"
		dashboard.save(failOnError: true)

		service.permissionService = new PermissionService()

		when:
		service.authorizedGetById(Dashboard, "dashboard-id", me, Permission.Operation.DASHBOARD_EDIT)

		then:
		def e = thrown(NotPermittedException)
		e.message == "me@me.com does not have permission to dashboard_edit Dashboard (id dashboard-id)"
	}

	void "authorizedGetById() returns domain object if it exists and user has required permission"() {
		User me = new User(username: "me@me.com")
		Dashboard dashboard = new Dashboard(name: "dashboard")
		dashboard.id = "dashboard-id"
		dashboard.save(failOnError: true)

		service.permissionService = Stub(PermissionService) // replace verify() with nop method

		when:
		def result = service.authorizedGetById(Dashboard, "dashboard-id", me, Permission.Operation.DASHBOARD_EDIT)

		then:
		result == dashboard
	}
}
