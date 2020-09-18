package com.unifina.security

import spock.lang.Specification

class ApiKeyConverterSpec extends Specification {
	void "createEthereumPrivateKey"() {
		when:
		String converted = ApiKeyConverter.createEthereumPrivateKey("mock-api-key")
		then:
		true
		converted == "0x003806b4ddedf17eddb217e191e02a98dd057d4f223c5e0302c5d00fb2b604c9"
	}
}
