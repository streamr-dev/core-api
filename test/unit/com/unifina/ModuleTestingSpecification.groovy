package com.unifina

import com.unifina.datasource.DataSource
import com.unifina.domain.security.SecUser
import com.unifina.signalpath.AbstractSignalPathModule
import com.unifina.signalpath.SignalPath
import com.unifina.utils.Globals
import com.unifina.utils.GlobalsFactory
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin

/**
 * Spec base class with helpers for creating and setting up testing for AbstractSignalPathModules
 */
@TestMixin(GrailsUnitTestMixin)
class ModuleTestingSpecification extends BeanMockingSpecification {
	protected <T extends AbstractSignalPathModule> T setupModule(T module, Map moduleConfig = [:], SignalPath parentSignalPath = new SignalPath(true), Globals globals = mockGlobals()) {
		module.globals = globals
		module.init()
		module.configure(moduleConfig)
		module.setParentSignalPath(parentSignalPath)
		return module
	}

	protected Globals mockGlobals(Map context=[:], SecUser user = new SecUser(timezone: "UTC")) {
		Globals globals = GlobalsFactory.createInstance(context, grailsApplication, user)
		globals.setDataSource(Mock(DataSource))
		globals.init()
		globals.time = new Date(0)
		return globals
	}
}
