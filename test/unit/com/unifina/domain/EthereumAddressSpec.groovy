package com.unifina.domain

import spock.lang.Specification

class EthereumAddressSpec extends Specification {
	void "toString() returns checksum form"() {
		when:
		EthereumAddress address = new EthereumAddress('0x0089d53f703f7e0843953d48133f74ce247184c2')
		then:
		address.toString() == "0x0089d53F703f7E0843953D48133f74cE247184c2"
	}

	void "equals and hashCode"() {
		when:
		EthereumAddress address1 = new EthereumAddress('0x0089d53f703f7e0843953d48133f74ce247184c2')
		EthereumAddress address2 = new EthereumAddress('0x0089D53F703F7E0843953D48133F74CE247184C2')
		then:
		address1.equals(address2)
		address1.hashCode() == address2.hashCode()
	}
}
