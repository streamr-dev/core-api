package com.unifina

import com.unifina.datasource.DataSource
import com.unifina.domain.User
import com.unifina.signalpath.AbstractSignalPathModule
import com.unifina.signalpath.SignalPath
import com.unifina.utils.Globals

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
		if (parentSignalPath != null) {
			parentSignalPath.globals = globals
		}
		return module
	}

	protected Globals mockGlobals(Map context=[:], User user = new User(username: 'user', timezone: "UTC"), Globals.Mode mode = Globals.Mode.REALTIME) {
		if (mode == Globals.Mode.HISTORICAL) {
			context.beginDate = context.beginDate ?: new Date().getTime()
			context.endDate = context.endDate ?: new Date().getTime()
		}

		Globals globals = new Globals(context, user, mode, Mock(DataSource))
		globals.time = new Date(0)
		return globals
	}
}
