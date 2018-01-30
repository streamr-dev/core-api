package com.unifina.signalpath.streams

import com.unifina.ModuleTestingSpecification
import com.unifina.domain.data.Stream
import com.unifina.domain.security.SecUser
import com.unifina.service.PermissionService
import com.unifina.service.StreamService
import com.unifina.signalpath.streams.GetOrCreateStream.NameFilteringClosure
import com.unifina.utils.testutils.ModuleTestHelper
import grails.test.mixin.Mock

@Mock(SecUser)
class GetOrCreateStreamSpec extends ModuleTestingSpecification {

	GetOrCreateStream module
	StreamService streamService
	PermissionService permissionService
	Stream streamExists
	Stream streamExistsWithFields

	def setup() {
		streamExists = new Stream(name: "exists")
		streamExists.id = "666-666-666-999"

		streamExistsWithFields = new Stream(name: "exists-with-fields")
		streamExistsWithFields.id = "111-333-111"
		streamExistsWithFields.config = "{'fields': [{'name': 'x', 'type': 'number'}, {'name': 'y', 'type': 'string'}]}"

		streamService = Mock(StreamService)
		streamService.createStream(_, _) >> {params, user ->
			Stream s = new Stream()
			s.id = params.name
			return s
		}
		mockBean(StreamService, streamService)

		permissionService = Mock(PermissionService)
		permissionService.get(_, _, _, _, _) >> {clazz, user, ops, includeAnonymous, NameFilteringClosure filter ->
			if (filter.getName() == "exists") {
				return [streamExists]
			} else if (filter.getName() == "exists-with-fields") {
				return [streamExistsWithFields]
			} else {
				return []
			}
		}
		mockBean(PermissionService, permissionService)

		module = setupModule(new GetOrCreateStream())
	}

	def cleanup() {
		cleanupMockBeans()
	}

	void "GetOrCreateStreamSpec works as expected"() {
		Map inputValues = [
			name: ["doesnotexist", "exists", "doesnotexist2", "exists-with-fields", "doesnotexist"],
			description: ["test-stream"] * 5,
			fields: [[a: "boolean", b: "string"]] * 5
		]
		Map outputValues = [
			"stream": ["doesnotexist", "666-666-666-999", "doesnotexist2", "111-333-111", "doesnotexist"],
			"created": [true, false, true, false, false]
//			"fields": [fields, [:], fields, [x: "number", y: "string"]]
		]

		expect:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}

	void "found streams are cached"() {
		Map inputValues = [
				name: ["exists", "exists"],
				description: ["test-stream"] * 2,
				fields: [[a: "boolean", b: "string"]] * 2
		]
		Map outputValues = [
				"stream": ["666-666-666-999", "666-666-666-999"],
				"created": [false, false]
		]

		when:
		boolean pass = new ModuleTestHelper.Builder(module, inputValues, outputValues)
				.serializationModes([ModuleTestHelper.SerializationMode.NONE].toSet())
				.test()

		then:
		pass
		1 * permissionService.get(_, _, _, _, _) >> {
			return [streamExists]
		}
		0 * streamService.createStream(_, _)
	}
}
