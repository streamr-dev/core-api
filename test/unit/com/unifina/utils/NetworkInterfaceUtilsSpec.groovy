package com.unifina.utils

import spock.lang.Specification

import static NetworkInterfaceUtils.*

class NetworkInterfaceUtilsSpec extends Specification {

	void setup() {
		GroovySpy(NetworkInterface, global: true)
	}

	void "isIpAddressOfCurrentNode() returns true if given ipAddress matches network interface address"() {
		def networkInterface = setUpNetworkInterface(false, false)

		when:
		def result = isIpAddressOfCurrentNode("6.6.6.6")

		then:
		result

		and:
		1 * NetworkInterface.getNetworkInterfaces() >> ([networkInterface] as Enumeration)
	}

	void "isIpAddressOfCurrentNode() returns false if given ipAddress matches virtual interface address"() {
		def networkInterface = setUpNetworkInterface(true, false)

		when:
		def result = isIpAddressOfCurrentNode("6.6.6.6")

		then:
		!result

		and:
		1 * NetworkInterface.getNetworkInterfaces() >> ([networkInterface] as Enumeration)
	}

	void "isIpAddressOfCurrentNode() returns false if given ipAddress matches point-to-point interface address"() {
		def networkInterface = setUpNetworkInterface(false, true)

		when:
		def result = isIpAddressOfCurrentNode("6.6.6.6")

		then:
		!result

		and:
		1 * NetworkInterface.getNetworkInterfaces() >> ([networkInterface] as Enumeration)
	}

	void "isIpAddressOfCurrentNode() returns false if given ipAddress does not match network interface address"() {
		def networkInterface = setUpNetworkInterface(false, false)

		when:
		def result = isIpAddressOfCurrentNode("7.7.7.7")

		then:
		!result

		and:
		1 * NetworkInterface.getNetworkInterfaces() >> ([networkInterface] as Enumeration)
	}

	private NetworkInterface setUpNetworkInterface(boolean virtual, boolean pointToPoint) {
		return GroovyStub(NetworkInterface) {
			getInetAddresses() >> ([Inet4Address.getByName("6.6.6.6")] as Enumeration)
			isVirtual() >> virtual
			isPointToPoint() >> pointToPoint
		}
	}
}
