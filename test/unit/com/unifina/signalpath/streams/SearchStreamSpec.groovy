package com.unifina.signalpath.streams

import com.unifina.ModuleTestingSpecification
import com.unifina.domain.data.Stream
import com.unifina.domain.security.SecUser
import com.unifina.service.PermissionService
import com.unifina.utils.Globals
import com.unifina.utils.testutils.ModuleTestHelper
import grails.test.mixin.Mock

@Mock([SecUser])
class SearchStreamSpec extends ModuleTestingSpecification {

	SearchStream module
	PermissionService permissionService

	def setup() {
		module = setupModule(new SearchStream())

		Stream streamExists = new Stream(name: "exists")
		streamExists.id = "666-666-666-999"

		Stream streamExistsWithFields = new Stream(name: "exists-with-fields")
		streamExistsWithFields.id = "111-333-111"
		streamExistsWithFields.config = "{'fields': [{'name': 'a', 'type': 'number'}, {'name': 'b', 'type': 'string'}]}"

		permissionService = mockBean(PermissionService.class, Mock(PermissionService))
		permissionService.get(_, _, _, _, _) >> { clazz, user, ops, includeAnonymous, SearchStream.NameFilteringClosure filter ->
			if (filter.getName() == "exists") {
				return [streamExists]
			} else if (filter.getName() == "exists-with-fields") {
				return [streamExistsWithFields]
			} else {
				return []
			}
		}
	}

	def cleanup() {
		cleanupMockBeans()
	}

	void "searchStream works as expected"() {
		when:
		Map inputValues = [
			name: ["doesnotexist", "exists", "doesnotexist2", "exists-with-fields"]
		]
		Map outputValues = [
			"stream": [null, "666-666-666-999", "666-666-666-999", "111-333-111"],
			"found": [false, true, false, true],
			"fields": [null, null, null, [a: "number", b: "string"]]
		]

		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues)
			.test()
	}
}
