package com.unifina.service

import com.unifina.domain.security.IntegrationKey
import com.unifina.domain.security.SecUser
import grails.converters.JSON
import groovy.transform.CompileStatic
import org.apache.commons.codec.binary.Hex
import org.codehaus.groovy.grails.web.json.JSONObject

import org.ethereum.crypto.ECKey

class EthereumIntegrationKeyService {

	IntegrationKey createEthereumAccount(SecUser user, String name, String privateKey) {
		privateKey = trimPrivateKey(privateKey)

		try {
			IntegrationKey key = new IntegrationKey()
			key.setName(name)
			key.setJson(([
					privateKey: privateKey,
					address   : "0x" + getPublicKey(privateKey)
			] as JSON).toString())
			key.setUser(user)
			key.setService(IntegrationKey.Service.ETHEREUM)
			key.save(flush: true, failOnError: true)
			return key
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("Private key must be a valid hex string!")
		}
	}

	List<IntegrationKey> getAllKeysForUser(SecUser user) {
		IntegrationKey.findAllByServiceAndUser(IntegrationKey.Service.ETHEREUM, user)
	}

	@CompileStatic
	private static String trimPrivateKey(String privateKey) {
		privateKey = privateKey.trim()
		if (privateKey.startsWith("0x")) {
			privateKey = privateKey.substring(2)
		}
		return privateKey
	}

	@CompileStatic
	private static String getPublicKey(String privateKey) {
		BigInteger pk = new BigInteger(privateKey, 16)
		ECKey key = ECKey.fromPrivate(pk)
		String publicKey = Hex.encodeHexString(key.getAddress())

		return publicKey
	}
}
