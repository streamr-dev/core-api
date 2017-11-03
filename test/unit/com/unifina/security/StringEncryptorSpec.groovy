package com.unifina.security

import org.apache.commons.codec.binary.Hex
import spock.lang.Specification

class StringEncryptorSpec extends Specification {
	void "can encrypt and then decrypt"() {
		def salt = Hex.decodeHex("060EE583828D7C7BAC85757EDC966974".toCharArray())
		def encrpytor = new StringEncryptor("password")

		when:
		String encrypted = encrpytor.encrypt("Hello world!", salt)
		then:
		encrypted != "Hello world!"
		and:
		encrpytor.decrypt(encrypted, salt) == "Hello world!"
	}
}
