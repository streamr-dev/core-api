package com.unifina.signalpath.variadic

import com.unifina.signalpath.AbstractSignalPathModule
import com.unifina.signalpath.Endpoint
import com.unifina.signalpath.TimeSeriesInput
import spock.lang.Specification

class VariadicEndpointSpec extends Specification {

	def variadicEndpoint = new VariadicEndpoint(null, new InputInstantiator.SimpleObject(), "names") {

		@Override
		def void attachToModule(AbstractSignalPathModule owner, Endpoint endpoint) {}

		@Override
		def void handlePlaceholder(Endpoint placeholderEndoint) {}

		@Override
		def String getDisplayName() {
			return "nameprefix"
		}
	}

	def "onConfiguration() with empty config initializes no endpoint and single placeholder"() {
		when:
		variadicEndpoint.onConfiguration([:])
		then:
		variadicEndpoint.endpoints.size() == 0
		variadicEndpoint.placeholder != null
	}

	def "onConfiguration() with config initializes existing endpoints and single placeholder"() {
		when:
		variadicEndpoint.onConfiguration([
			names: ["endpoint-29fka8d", "endpoint-lmg92jk"]
		])
		then:
		variadicEndpoint.endpoints.size() == 2
		variadicEndpoint.endpoints[0].name == "endpoint-29fka8d"
		variadicEndpoint.endpoints[1].name == "endpoint-lmg92jk"
		variadicEndpoint.placeholder.name.startsWith("endpoint-")
	}

	def "endpoints' display names contain prefix followed by index"() {
		when:
		variadicEndpoint.onConfiguration([
			names: ["endpoint-29fka8d", "endpoint-lmg92jk"]
		])
		then:
		variadicEndpoint.endpoints*.displayName == (1..2).collect { "nameprefix$it" }
	}

	def "placeholder's display name contains prefix followed by index"() {
		when:
		variadicEndpoint.onConfiguration([
			names: ["endpoint-29fka8d", "endpoint-lmg92jk"]
		])
		then:
		variadicEndpoint.placeholder.displayName == "nameprefix3"
	}

	def "getConfiguration() provides current number of endpoints along with their names"() {
		variadicEndpoint.onConfiguration([
			names: ["endpoint-29fka8d", "endpoint-lmg92jk"]
		])
		def config = [:]

		when:
		variadicEndpoint.getConfiguration(config)

		then:
		config.keySet() == ["names"] as Set
		config.names.size() == 2
		config.names.every { it.startsWith("endpoint-") }
	}
}
