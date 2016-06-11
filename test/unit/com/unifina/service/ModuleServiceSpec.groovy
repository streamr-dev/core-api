package com.unifina.service

import com.unifina.domain.signalpath.Module
import com.unifina.domain.signalpath.ModuleCategory
import com.unifina.signalpath.AbstractSignalPathModule
import com.unifina.signalpath.SignalPath
import com.unifina.signalpath.simplemath.VariadicAddMulti
import com.unifina.utils.Globals
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import spock.lang.Specification

@TestFor(ModuleService)
@Mock(Module)
class ModuleServiceSpec extends Specification {

	def globals = Stub(Globals)

	def module = new Module(
			name: "Add",
			implementingClass: VariadicAddMulti.canonicalName,
			jsModule: "jsModule",
			type: "type",
			category: new ModuleCategory(name: "category")
	)

	def "it instantiates AbstractSignalPathModule according to Module"() {
		when:
		AbstractSignalPathModule spm = service.getModuleInstance(module, null, null, globals)

		then:
		spm != null
		spm.name == "Add"
		spm.globals == globals
		spm.domainObject == module
		spm.parentSignalPath == null
		spm.getConfiguration().inputs.size() == 2
	}

	def "it instantiates AbstractSignalPathModule according to Module, config and parentSignalPath"() {
		def config = [:]
		def signalPath = new SignalPath()

		when:
		AbstractSignalPathModule spm = service.getModuleInstance(module, config, signalPath, globals)

		then:
		spm != null
		spm.name == "Add"
		spm.globals == globals
		spm.domainObject == module
		spm.parentSignalPath == signalPath
		spm.getConfiguration().inputs.size() == 3 // Check that configure() called
	}

	def "it can get Module domain objects for given config"() {
		def savedModules = (1..3).collect {
			Module module = new Module(name: "module" + it)
			module.id = it
			module.save(validate: false)
		}

		expect:
		service.getModuleDomainObjects([]) == []
		service.getModuleDomainObjects([[id: 1], [id: 2], [id: 3]]) == savedModules
	}
}
