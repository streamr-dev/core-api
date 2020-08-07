package com.unifina

import com.unifina.domain.security.Key
import com.unifina.domain.security.User
import com.unifina.filters.MockAPIFilters
import com.unifina.security.AllowRole
import com.unifina.security.StreamrApi
import grails.test.mixin.TestMixin
import grails.test.mixin.web.FiltersUnitTestMixin
import grails.util.Holders

import java.lang.reflect.Method

@TestMixin(FiltersUnitTestMixin)
class ControllerSpecification extends BeanMockingSpecification {

	def setup() {
		mockFilters(MockAPIFilters)
	}

	def unauthenticated(Map arguments = [:], Closure callable) {
		checkFilter()
		MockAPIFilters.setUser(null)
		return withFilters(arguments, callable)
	}

	def authenticatedAs(User user, Map arguments = [:], Closure callable) {
		checkFilter()
		MockAPIFilters.setUser(user)
		def result = withFilters(arguments, callable)
		MockAPIFilters.setUser(null)
		return result
	}

	def authenticatedAs(Key key, Map arguments = [:], Closure callable) {
		checkFilter()
		MockAPIFilters.setKey(key)
		def result = withFilters(arguments, callable)
		MockAPIFilters.setKey(null)
		return result
	}

	Set<String> getInvalidAnnotations(Class controllerClass, Map<String, List<AllowRole>> actionRoles) {
		Set<String> invalidActionNames = new HashSet<String>()
		for (String actionName in actionRoles.keySet()) {
			Method action = controllerClass.getMethod(actionName)
			if (!validAnnotation(action, actionRoles.get(actionName))) {
				invalidActionNames.add(actionName)
			}
		}
		return invalidActionNames
	}

	private void checkFilter() {
		MockAPIFilters filter = Holders.getGrailsApplication().getMainContext().getBean(MockAPIFilters)
		if (!filter) {
			throw new RuntimeException("Add @Mock([MockAPIFilters]) to your test class!")
		}
	}

	private boolean validAnnotation(Method action, List<AllowRole> targetRoles) {
		List<AllowRole> roles = action.getAnnotation(StreamrApi).allowRoles().toList()
		roles == targetRoles
	}

}
