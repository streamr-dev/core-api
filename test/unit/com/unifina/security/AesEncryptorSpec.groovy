package com.unifina.security

import spock.lang.Specification

class AesEncryptorSpec extends Specification {
	void "can encrypt and then decrypt"() {
		def encrpytor = new AesEncryptor("password", "060EE583828D7C7BAC85757EDC966974")
		when:
		String encrypted = encrpytor.encrypt("Hello world!")
		then:
		encrypted != "Hello world!"
		and:
		encrpytor.decrypt(encrypted) == "Hello world!"
	}
}
