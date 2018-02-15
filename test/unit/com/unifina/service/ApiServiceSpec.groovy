package com.unifina.service

import com.unifina.api.DashboardListParams
import com.unifina.api.ListParams
import com.unifina.api.ValidationException
import com.unifina.domain.dashboard.Dashboard
import com.unifina.domain.security.Permission
import com.unifina.domain.security.SecUser
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.web.ControllerUnitTestMixin
import grails.web.CamelCaseUrlConverter
import org.codehaus.groovy.grails.web.mapping.DefaultLinkGenerator
import org.codehaus.groovy.grails.web.mapping.LinkGenerator
import spock.lang.Specification

import javax.servlet.http.HttpServletResponse

@TestMixin(ControllerUnitTestMixin) // "as JSON" converter
@TestFor(ApiService)
class ApiServiceSpec extends Specification {

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
		service.addLinkHintToHeader(params, 100, [action: "index", controller: "dashboardApi"], response)
		then:
		1 * response.addHeader("Link", '<http://localhost:8080/api/v1/dashboards?max=100&offset=250&publicAccess=true&name=dashboard>; rel="more"')
	}

}
