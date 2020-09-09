package com.unifina.utils

import org.web3j.crypto.ECKeyPair
import org.web3j.crypto.Keys
import grails.compiler.GrailsCompileStatic
import org.apache.commons.lang3.StringUtils

@GrailsCompileStatic
class PrivateKeyGenerator {

	private static final int LENGTH = 64

	static String generate() {
		ECKeyPair keyPair = Keys.createEcKeyPair()
		BigInteger decimal = keyPair.getPrivateKey()
		return StringUtils.leftPad(decimal.toString(16), LENGTH, "0")
	}
}