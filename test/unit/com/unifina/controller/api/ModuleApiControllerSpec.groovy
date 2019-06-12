package com.unifina.controller.api

import com.unifina.ControllerSpecification
import com.unifina.api.NotPermittedException
import com.unifina.domain.security.Key
import com.unifina.domain.security.Permission
import com.unifina.domain.security.SecUser
import com.unifina.domain.signalpath.Module
import com.unifina.domain.signalpath.ModulePackage
import com.unifina.service.PermissionService
import grails.test.mixin.Mock
import grails.test.mixin.TestFor

@TestFor(ModuleApiController)
@Mock([SecUser, Module, Key, ModulePackage])
class ModuleApiControllerSpec extends ControllerSpecification {

	SecUser me
	ModulePackage modulePackage
	Module module

	def setup() {
		me = new SecUser(id: 1).save(validate: false)
		modulePackage = new ModulePackage().save(validate: false)
		module = new Module(modulePackage: modulePackage).save(validate: false)

		def key = new Key(name: "key", user: me)
		key.id = "myApiKey"
		key.save(failOnError: true, validate: true)

		assert SecUser.count() == 1
		assert ModulePackage.count() == 1
		assert Module.count() == 1
	}

	void "help() must render the help json of the module"() {
		controller.permissionService = Mock(PermissionService)
		module.jsonHelp = "help"

		when:
		params.id = module.id
		authenticatedAs(me) { controller.help() }

		then:
		1 * controller.permissionService.check(me, module.modulePackage, Permission.Operation.READ) >> true
		response.text == "help"
	}

	void "help() must render an empty json object if module has no help"() {
		controller.permissionService = Mock(PermissionService)
		module.jsonHelp = null

		when:
		params.id = module.id
		authenticatedAs(me) { controller.help() }

		then:
		1 * controller.permissionService.check(me, module.modulePackage, Permission.Operation.READ) >> true
		response.text == "{}"
	}

	void "help() must throw exception for a module I cannot access"() {
		controller.permissionService = Mock(PermissionService)

		when:
		params.id = module.id
		authenticatedAs(me) { controller.help() }

		then:
		thrown(NotPermittedException)
		1 * controller.permissionService.check(me, module.modulePackage, Permission.Operation.READ) >> false
	}
}
