package com.unifina.controller.api

import com.unifina.ControllerSpecification
import com.unifina.domain.security.Key
import com.unifina.domain.security.SecUser
import com.unifina.domain.signalpath.Module
import grails.test.mixin.Mock
import grails.test.mixin.TestFor

@TestFor(ModuleApiController)
@Mock([SecUser, Module, Key])
class ModuleApiControllerSpec extends ControllerSpecification {

	SecUser me
	Module module

	def setup() {
		me = new SecUser(id: 1).save(validate: false)
		module = new Module().save(validate: false)

		def key = new Key(name: "key", user: me)
		key.id = "myApiKey"
		key.save(failOnError: true, validate: true)

		assert SecUser.count() == 1
		assert Module.count() == 1
	}

	void "help() must render the help json of the module"() {
		module.jsonHelp = "help"

		when:
		params.id = module.id
		authenticatedAs(me) { controller.help() }

		then:
		response.text == "help"
	}

	void "help() must render an empty json object if module has no help"() {
		module.jsonHelp = null

		when:
		params.id = module.id
		authenticatedAs(me) { controller.help() }

		then:
		response.text == "{}"
	}
}
