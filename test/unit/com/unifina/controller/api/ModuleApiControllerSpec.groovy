package com.unifina.controller.api

import com.unifina.api.NotPermittedException
import com.unifina.domain.security.Permission
import com.unifina.domain.security.SecUser
import com.unifina.domain.signalpath.Module
import com.unifina.domain.signalpath.ModulePackage
import com.unifina.filters.UnifinaCoreAPIFilters
import com.unifina.service.PermissionService
import com.unifina.service.UserService
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import grails.test.mixin.web.FiltersUnitTestMixin
import spock.lang.Specification

@TestFor(ModuleApiController)
@Mixin(FiltersUnitTestMixin)
@Mock([SecUser, Module, ModulePackage, UnifinaCoreAPIFilters, UserService])
class ModuleApiControllerSpec extends Specification {

	SecUser me
	ModulePackage modulePackage
	Module module

	void setup() {
		me = new SecUser(id: 1, apiKey: "myApiKey").save(validate: false)
		modulePackage = new ModulePackage().save(validate: false)
		module = new Module(modulePackage: modulePackage).save(validate: false)

		assert SecUser.count() == 1
		assert ModulePackage.count() == 1
		assert Module.count() == 1
	}

	void "help() must render the help json of the module"() {
		controller.permissionService = Mock(PermissionService)
		module.jsonHelp = "help"

		when:
		request.addHeader("Authorization", "Token myApiKey")
		params.id = module.id
		request.requestURI = "/api/v1/modules/$module.id/help"
		withFilters(action: "help") {
			controller.help()
		}

		then:
		1 * controller.permissionService.check(me, module.modulePackage, Permission.Operation.READ) >> true
		response.text == "help"
	}

	void "help() must render an empty json object if module has no help"() {
		controller.permissionService = Mock(PermissionService)
		module.jsonHelp = null

		when:
		request.addHeader("Authorization", "Token myApiKey")
		params.id = module.id
		request.requestURI = "/api/v1/modules/$module.id/help"
		withFilters(action: "help") {
			controller.help()
		}

		then:
		1 * controller.permissionService.check(me, module.modulePackage, Permission.Operation.READ) >> true
		response.text == "{}"
	}

	void "help() must throw exception for a module I cannot access"() {
		controller.permissionService = Mock(PermissionService)

		when:
		request.addHeader("Authorization", "Token myApiKey")
		params.id = module.id
		request.requestURI = "/api/v1/modules/$module.id/help"
		withFilters(action: "help") {
			controller.help()
		}

		then:
		thrown(NotPermittedException)
		1 * controller.permissionService.check(me, module.modulePackage, Permission.Operation.READ) >> false
	}
}
