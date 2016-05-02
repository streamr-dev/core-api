package com.unifina.signalpath.variadic

import com.unifina.signalpath.AbstractSignalPathModule
import com.unifina.signalpath.Endpoint
import com.unifina.signalpath.TimeSeriesInput
import spock.lang.Specification

class VariadicEndpointSpec extends Specification {

	def variadicEndpoint = new VariadicEndpoint(null, new InputInstantiator.SimpleObject(), "num", "names", 5) {

		@Override
		def void attachToModule(AbstractSignalPathModule owner, Endpoint endpoint) {}

		@Override
		def String getDisplayName() {
			return "nameprefix"
		}
	}

	def "onConfiguration() with empty config initializes default number of endpoints"() {
		when:
		variadicEndpoint.onConfiguration([:])
		then:
		variadicEndpoint.endpoints.size() == 5
	}

	def "onConfiguration() with existing config initializes expected number of endpoints"() {
		when:
		variadicEndpoint.onConfiguration([
			options: [
			    num: [value: 13, type: "int"]
			]
		])
		then:
		variadicEndpoint.endpoints.size() == 13
	}

	def "endpoints' display names contain prefix followed by index"() {
		when:
		variadicEndpoint.onConfiguration([:])
		then:
		variadicEndpoint.endpoints*.displayName == (1..5).collect { "nameprefix$it" }
	}

	def "single endpoint's display name contains only prefix without index"() {
		when:
		variadicEndpoint.onConfiguration([
			options: [
				num: [value: 1, type: "int"]
			]
		])
		then:
		variadicEndpoint.endpoints*.displayName == ["nameprefix"]
	}

	def "onConfiguration() with existing config re-uses endpoint names for previously seen endpoints"() {
		when:
		variadicEndpoint.onConfiguration([
			options: [
				num: [value: 6, type: "int"],
			],
			names: ["aa", "bb", "cc", "dd"]
		])
		then:
		variadicEndpoint.endpoints.size() == 6
		variadicEndpoint.endpoints.subList(0, 4)*.name == ["aa", "bb", "cc", "dd"]
		variadicEndpoint.endpoints.subList(4, 6).name.every { it.startsWith("endpoint-") }
	}

	def "getConfiguration() provides current number of endpoints along with their names"() {
		variadicEndpoint.onConfiguration([:])
		def config = [:]

		when:
		variadicEndpoint.getConfiguration(config)

		then:
		config.keySet() == ["options", "names"] as Set

		and:
		config.options == [
		    num: [value: 5, type: "int"]
		]

		and:
		config.names.size() == 5
		config.names.every { it.startsWith("endpoint-") }
	}
}
