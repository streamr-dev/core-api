package com.unifina.security

import org.apache.commons.codec.binary.Hex
import spock.lang.Specification

class StringEncryptorSpec extends Specification {

	void "can encrypt and then decrypt"() {
		def salt = Hex.decodeHex("060EE583828D7C7BAC85757EDC966974".toCharArray())
		def encryptor = new StringEncryptor("password")

		when:
		// NOTE: if the below line throws java.security.InvalidKeyException: Illegal key size,
		// you need to install the Java Cryptography Extension (JCE) Unlimited Strength Jurisdiction Policy Files 8
		// from http://www.oracle.com/technetwork/java/javase/downloads/jce8-download-2133166.html
		String encrypted = encryptor.encrypt("Hello world!", salt)
		then:
		encrypted != "Hello world!"
		and:
		encryptor.decrypt(encrypted, salt) == "Hello world!"
	}

}
