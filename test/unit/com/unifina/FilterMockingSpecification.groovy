package com.unifina

import com.unifina.domain.security.Key
import com.unifina.domain.security.SecUser
import com.unifina.filters.MockAPIFilters
import grails.test.mixin.TestMixin
import grails.test.mixin.web.FiltersUnitTestMixin
import grails.util.Holders

@TestMixin(FiltersUnitTestMixin)
class FilterMockingSpecification extends BeanMockingSpecification {

	def setup() {
		mockFilters(MockAPIFilters)
	}

	def unauthenticated(Map arguments = [:], Closure callable) {
		checkFilter()
		MockAPIFilters.setUser(null)
		return withFilters(arguments, callable)
	}

	def authenticatedAs(SecUser user, Map arguments = [:], Closure callable) {
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

	private void checkFilter() {
		MockAPIFilters filter = Holders.getGrailsApplication().getMainContext().getBean(MockAPIFilters)
		if (!filter) {
			throw new RuntimeException("Add @Mock([MockAPIFilters]) to your test class!")
		}
	}

}
