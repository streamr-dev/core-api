package com.streamr.core.domain
import com.streamr.core.domain.EthereumAddress
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

	void "fromPrivateKey"() {
		when:
		EthereumAddress address1 = EthereumAddress.fromPrivateKey('0x0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF')
		EthereumAddress address2 = EthereumAddress.fromPrivateKey('0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF')
		then:
		address1.toString() == "0xFCAd0B19bB29D4674531d6f115237E16AfCE377c"
		address2.toString() == "0xFCAd0B19bB29D4674531d6f115237E16AfCE377c"
	}
}
