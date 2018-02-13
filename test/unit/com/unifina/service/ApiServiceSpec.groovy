package com.unifina.service

import com.unifina.api.DashboardListParams
import com.unifina.api.ListParams
import com.unifina.domain.dashboard.Dashboard
import com.unifina.domain.security.Permission
import com.unifina.domain.security.SecUser
import grails.test.mixin.TestFor
import spock.lang.Specification

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

	void "list() invokes listParams#createListCriteria and passes returned closure to permissionService#get"() {
		def permissionService = service.permissionService = Mock(PermissionService)

		SecUser me = new SecUser(username: "me@me.com")
		ListParams listParams = Mock(ListParams)

		when:
		service.list(Dashboard, listParams, me)

		then:
		1 * permissionService.get(_, _, _, _, { it() == "see me?" })
		1 * listParams.createListCriteria() >> { { a -> "see me?" } }
	}
}
