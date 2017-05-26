package com.unifina.service

import com.unifina.domain.security.IntegrationKey
import com.unifina.domain.security.SecUser
import grails.converters.JSON
import org.apache.commons.codec.binary.Hex
import org.codehaus.groovy.grails.web.json.JSONObject

import org.ethereum.crypto.ECKey

class EthereumIntegrationKeyService {

	IntegrationKey createEthereumAccount(SecUser user, String name, JSONObject json) {
		String privateKey = trimPrivateKey((String) json.get("privateKey"))

		try {
			IntegrationKey account = new IntegrationKey()
			account.setName(name)
			account.setJson(([
					privateKey: privateKey,
					address : getPublicKey(privateKey)
			] as JSON).toString())
			account.setUser(user)
			account.setService(IntegrationKey.Service.ETHEREUM)
			account.save(flush: true, failOnError: true)
			return account
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("Private key must be a valid hex string!")
		}
	}

	private static String trimPrivateKey(String privateKey) {
		if (privateKey.startsWith("0x")) {
			privateKey = privateKey.split("0x")[1]
		}
		return privateKey
	}

	private String getPublicKey(String privateKey) {
		BigInteger pk = new BigInteger(privateKey, 16)
		ECKey key = ECKey.fromPrivate(pk)
		String publicKey = Hex.encodeHexString(key.getAddress())

		return publicKey
	}
}
