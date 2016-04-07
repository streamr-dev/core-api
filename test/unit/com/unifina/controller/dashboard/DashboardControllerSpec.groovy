package com.unifina.controller.dashboard

import com.unifina.domain.dashboard.Dashboard
import com.unifina.domain.dashboard.DashboardItem
import com.unifina.domain.security.SecUser
import com.unifina.service.PermissionService
import grails.plugin.springsecurity.SpringSecurityService
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import spock.lang.Specification

@TestFor(DashboardController)
@Mock([SecUser, Dashboard, DashboardItem])
class DashboardControllerSpec extends Specification {

	SecUser me
	Dashboard dash

    def setup() {
		controller.springSecurityService = Stub(SpringSecurityService) {
			getCurrentUser() >> me
		}
		controller.permissionService = Stub(PermissionService) {
			check(_, _, _) >> true
		}

		me = new SecUser(username: "me", password: "foo", apiKey: "apiKey", apiSecret: "apiSecret").save(validate:false)
		dash = new Dashboard(name:"test", user:me)
		dash.addToItems(new DashboardItem(title:"item1"))
		dash.addToItems(new DashboardItem(title:"item2"))
		dash.addToItems(new DashboardItem(title:"item3"))
		dash.save(validate:false)
    }

	void "test setup"() {
		expect:
		Dashboard.count == 1
		DashboardItem.count == 3
	}

    void "deleting Dashboard also deletes DashboardItems"() {
		when:
		params.id = dash.id
		controller.delete()
		//dash.delete(flush:true)
		then:
		Dashboard.count == 0
		DashboardItem.count == 0
    }
}
