package com.unifina.signalpath.variadic

import com.unifina.signalpath.AbstractSignalPathModule
import com.unifina.signalpath.Endpoint
import com.unifina.signalpath.TimeSeriesInput
import spock.lang.Specification

class VariadicEndpointSpec extends Specification {

	def variadicEndpoint = new VariadicEndpoint(null, "numOfEndpoints", 5) {
		@Override
		def Endpoint makeAndAttachNewEndpoint(AbstractSignalPathModule owner) {
			return new TimeSeriesInput(owner, null)
		}

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

	def "onConfiguration() with existing config initializes config's number of endpoints"() {
		when:
		variadicEndpoint.onConfiguration([
			options: [
			    numOfEndpoints: [value: 13, type: "int"]
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
				numOfEndpoints: [value: 1, type: "int"]
			]
		])
		then:
		variadicEndpoint.endpoints*.displayName == ["nameprefix"]
	}

	def "getConfiguration() provides current number of endpoints"() {
		variadicEndpoint.onConfiguration([:])
		def config = [:]

		when:
		variadicEndpoint.getConfiguration(config)
		then:
		config == [options: [
		    numOfEndpoints: [value: 5, type: "int"]
		]]
	}
}
