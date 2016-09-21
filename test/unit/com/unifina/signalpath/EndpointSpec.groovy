package com.unifina.signalpath

import spock.lang.Specification

class EndpointSpec extends Specification {
	def owner = Mock(AbstractSignalPathModule)

	def setup() {
		owner.getName() >> "owner"
		owner.drivingInputs = new HashSet<>()
	}

	def "endpoint must report its type class correctly"() {
		Endpoint ep = new Endpoint1(owner, "test", "Integer")
		expect:
		ep.getTypeClass() == Integer.class
	}

	def "endpoint subclass must report its type class correctly"() {
		Endpoint ep = new Endpoint2(owner, "test", "Integer")
		expect:
		ep.getTypeClass() == Integer.class
	}

	class Endpoint1 extends Endpoint<Integer> {

		Endpoint1(AbstractSignalPathModule owner, String name, String typeName) {
			super(owner, name, typeName)
		}

		@Override
		boolean isConnected() {
			return false
		}

		@Override
		Integer getValue() {
			return null
		}

		@Override
		void clear() {

		}

		@Override
		void disconnect() {

		}
	}

	class Endpoint2 extends Endpoint1 {

		Endpoint2(AbstractSignalPathModule owner, String name, String typeName) {
			super(owner, name, typeName)
		}
	}

}
