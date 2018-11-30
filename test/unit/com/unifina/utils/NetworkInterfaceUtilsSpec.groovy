package com.unifina.utils

import spock.lang.Specification

import static NetworkInterfaceUtils.*

class NetworkInterfaceUtilsSpec extends Specification {

	void setup() {
		GroovySpy(NetworkInterface, global: true)
	}

	void "getIpAddress() returns a configured IP address if given"() {
		expect:
		getIPAddress(setConfiguredIp("1.2.3.4")) == "1.2.3.4"
	}

	void "getIpAddress() returns the first IPv4 address it finds"() {
		List interfaces = [
			setUpNetworkInterface(true, false),
			setUpNetworkInterface(false, true),
			setUpNetworkInterface(false, false,
			[
				Inet6Address.getByName("1080:0:0:0:8:800:200C:417A"),
			]),
			setUpNetworkInterface(false, false,
			[
				Inet6Address.getByName("1080:0:0:0:8:800:200C:417B"),
				Inet4Address.getByName("5.5.5.5")
			])
		]

		when:
		String result = getIPAddress([:])

		then:
		1 * NetworkInterface.getNetworkInterfaces() >> Collections.enumeration(interfaces)
		result == "5.5.5.5"
	}

	void "getIpAddress() falls back to 127.0.0.1 if it can't find other suitable interfaces"() {
		when:
		String result = getIPAddress([:])

		then:
		1 * NetworkInterface.getNetworkInterfaces() >> Collections.enumeration([])
		result == "127.0.0.1"
	}

	void "isIpAddressOfCurrentNode() returns true if given ipAddress matches network interface address"() {
		def networkInterface = setUpNetworkInterface(false, false)

		when:
		def result = isIpAddressOfCurrentNode("6.6.6.6", [:])

		then:
		result

		and:
		1 * NetworkInterface.getNetworkInterfaces() >> Collections.enumeration([networkInterface])
	}

	void "isIpAddressOfCurrentNode() returns false if given ipAddress matches virtual interface address"() {
		def networkInterface = setUpNetworkInterface(true, false)

		when:
		def result = isIpAddressOfCurrentNode("6.6.6.6", [:])

		then:
		!result

		and:
		1 * NetworkInterface.getNetworkInterfaces() >> Collections.enumeration([networkInterface])
	}

	void "isIpAddressOfCurrentNode() returns false if given ipAddress matches point-to-point interface address"() {
		def networkInterface = setUpNetworkInterface(false, true)

		when:
		def result = isIpAddressOfCurrentNode("6.6.6.6", [:])

		then:
		!result

		and:
		1 * NetworkInterface.getNetworkInterfaces() >> Collections.enumeration([networkInterface])
	}

	void "isIpAddressOfCurrentNode() returns false if given ipAddress does not match network interface address"() {
		def networkInterface = setUpNetworkInterface(false, false)

		when:
		def result = isIpAddressOfCurrentNode("7.7.7.7", [:])

		then:
		!result

		and:
		1 * NetworkInterface.getNetworkInterfaces() >> Collections.enumeration([networkInterface])
	}

	private Map setConfiguredIp(String ipAddress) {
		return [streamr: [node: [ip: ipAddress]]]
	}

	private NetworkInterface setUpNetworkInterface(boolean virtual, boolean pointToPoint,
												   Collection<Inet4Address> addresses = [Inet4Address.getByName("6.6.6.6")]) {
		return GroovyStub(NetworkInterface) {
			getInetAddresses() >> Collections.enumeration(addresses)
			isVirtual() >> virtual
			isPointToPoint() >> pointToPoint
		}
	}
}
