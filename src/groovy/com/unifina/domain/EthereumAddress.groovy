package com.unifina.domain

import org.web3j.crypto.Keys
import com.unifina.service.ValidationException
import grails.compiler.GrailsCompileStatic
import groovy.transform.EqualsAndHashCode
import org.apache.commons.codec.binary.Hex
import org.ethereum.crypto.ECKey

@GrailsCompileStatic
@EqualsAndHashCode
class EthereumAddress {
	private String value

	EthereumAddress(String value) {
		if (!EthereumAddressValidator.validate.call(value)) {
			throw new ValidationException("Address is not a valid Ethereum address")
		}
		this.value = Keys.toChecksumAddress(value)
	}

	public String toString() {
		return value
	}

	static EthereumAddress fromPrivateKey(String privateKey) {
		BigInteger pk = new BigInteger(privateKey, 16)
		ECKey key = ECKey.fromPrivate(pk)
		String publicKey = "0x" + Hex.encodeHexString(key.getAddress())
		return new EthereumAddress(publicKey)
	}
}