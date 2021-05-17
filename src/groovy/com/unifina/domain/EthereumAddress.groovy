package com.unifina.domain

import com.unifina.service.ValidationException
import grails.compiler.GrailsCompileStatic
import groovy.transform.EqualsAndHashCode
import org.web3j.crypto.Keys
import org.web3j.crypto.Sign
import org.web3j.utils.Numeric

@GrailsCompileStatic
@EqualsAndHashCode
class EthereumAddress {
	static final String ZERO = "0x0000000000000000000000000000000000000000"
	private static final String PREFIX = "0x"

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
		if (privateKey.startsWith(PREFIX)) {
			privateKey = privateKey.substring(PREFIX.length())
		}
		BigInteger pk = new BigInteger(privateKey, 16)
		BigInteger publicKey = Sign.publicKeyFromPrivate(pk);
		String address = Keys.getAddress(publicKey);
		return new EthereumAddress(Numeric.prependHexPrefix(address));
	}
}
