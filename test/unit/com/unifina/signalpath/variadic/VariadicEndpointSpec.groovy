package com.unifina.signalpath.variadic

import com.unifina.signalpath.AbstractSignalPathModule
import com.unifina.signalpath.Endpoint
import com.unifina.signalpath.TimeSeriesInput
import spock.lang.Specification

class VariadicEndpointSpec extends Specification {

	def module = Stub(AbstractSignalPathModule)
	def attachToModuleEndpoints = []
	def variadicEndpoint = new VariadicEndpoint(module, new InputInstantiator.SimpleObject(), 5) {

		@Override
		def void attachToModule(AbstractSignalPathModule owner, Endpoint endpoint) {
			assert owner == module
			attachToModuleEndpoints << endpoint
		}

		@Override
		def void furtherConfigurePlaceholder(Endpoint placeholder) {}

		@Override
		def String getDisplayName() {
			return "nameprefix"
		}

		@Override
		def String getJsClass() {
			return "jsClass"
		}
	}

	def "state right after instantiation"() {
		expect: "no endpoints"
		variadicEndpoint.getEndpoints().empty
		variadicEndpoint.getEndpointsIncludingPlaceholder().empty

	}

	def "state after onConfiguration() on fresh instance"() {
		when:
		variadicEndpoint.onConfiguration(null)

		then: "no 'non-placeholder' endpoints"
		variadicEndpoint.endpoints.empty

		and: "one appropriately named placeholder endpoint"
		variadicEndpoint.endpointsIncludingPlaceholder.size() == 1
		variadicEndpoint.endpointsIncludingPlaceholder.first().displayName == "nameprefix5"

		and: "one invocation of attachToModule"
		attachToModuleEndpoints == [variadicEndpoint.endpointsIncludingPlaceholder.first()]
	}

	def "state after 4 addEndpoint() calls followed by an onConfiguration() invocation"() {
		when:
		variadicEndpoint.addEndpoint("first-endpoint")
		variadicEndpoint.addEndpoint("second-endpoint")
		variadicEndpoint.addEndpoint("third-endpoint")
		variadicEndpoint.addEndpoint("placeholder-endpoint")
		variadicEndpoint.onConfiguration(null)

		List<Endpoint> allEps = variadicEndpoint.endpointsIncludingPlaceholder

		then: "3 'non-placeholder' endpoints"
		variadicEndpoint.endpoints.size() == 3

		and: "4 endpoints when including placeholder"
		allEps.size() == 4

		and: "4 + 1 (additional for placeholder) invocations of attachToModule"
		attachToModuleEndpoints == allEps + [allEps.last()]

		and: "endpoints appropriately named"
		allEps*.displayName == ["nameprefix5", "nameprefix6", "nameprefix7", "nameprefix8"]

		and: "endpoints appropriately configured"
		allEps*.getConfiguration()*.jsClass == ["jsClass", "jsClass", "jsClass", "jsClass"]
		allEps*.getConfiguration()*.variadic == [
			[isLast: false, index: 5],
			[isLast: false, index: 6],
			[isLast: false, index: 7],
			[isLast: true, index: 8]
		]
	}
}
