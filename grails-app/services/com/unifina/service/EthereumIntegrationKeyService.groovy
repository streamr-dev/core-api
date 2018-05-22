package com.unifina.service

import com.unifina.api.ApiException
import com.unifina.api.DuplicateNotAllowedException
import com.unifina.crypto.ECRecover
import com.unifina.domain.security.Challenge
import com.unifina.domain.security.IntegrationKey
import com.unifina.domain.security.SecUser
import com.unifina.security.StringEncryptor
import grails.compiler.GrailsCompileStatic
import grails.converters.JSON
import groovy.transform.CompileStatic
import org.apache.commons.codec.DecoderException
import org.apache.commons.codec.binary.Hex
import org.ethereum.crypto.ECKey
import org.springframework.util.Assert

import javax.annotation.PostConstruct
import java.security.SignatureException

class EthereumIntegrationKeyService {

	def grailsApplication
	StringEncryptor encryptor
	SubscriptionService subscriptionService

	@PostConstruct
	void init() {
		String password = grailsApplication.config["streamr"]["encryption"]["password"]
		Assert.notNull(password, "streamr.encryption.password not set!")
		encryptor = new StringEncryptor(password)
	}

	IntegrationKey createEthereumAccount(SecUser user, String name, String privateKey) {
		privateKey = trimPrivateKey(privateKey)

		try {
			String publicKey = "0x" + getPublicKey(privateKey)
			String encryptedPrivateKey = encryptor.encrypt(privateKey, user.id.byteValue())
			return new IntegrationKey(
					name: name,
					user: user,
					service: IntegrationKey.Service.ETHEREUM,
					idInService: publicKey,
					json: ([
							privateKey: encryptedPrivateKey,
							address   : publicKey
					] as JSON).toString()
			).save(flush: true, failOnError: true)
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("Private key must be a valid hex string!")
		}
	}

	IntegrationKey createEthereumID(SecUser user, String name, String challengeID, String challenge, String signature) {
		def dbChallenge = Challenge.get(challengeID)
		def invalidChallenge = dbChallenge == null || challenge != dbChallenge.challenge
		if (invalidChallenge) {
			throw new ApiException(400, "INVALID_CHALLENGE", "challenge validation failed")
		}

		def message = challenge
		String address
		try {
			byte[] messageHash = ECRecover.calculateMessageHash(message)
			address = ECRecover.recoverAddress(messageHash, signature)
		} catch (SignatureException | DecoderException e) {
			throw new ApiException(400, "ADDRESS_RECOVERY_ERROR", e.message)
		}

		if (IntegrationKey.findByServiceAndIdInService(IntegrationKey.Service.ETHEREUM_ID, address) != null) {
			throw new DuplicateNotAllowedException("This Ethereum address is already associated with another Streamr user.")
		}

		dbChallenge.delete()

		IntegrationKey integrationKey = new IntegrationKey(
			name: name,
			user: user,
			service: IntegrationKey.Service.ETHEREUM_ID.toString(),
			idInService: address,
			json: ([
				address: new String(address)
			] as JSON).toString()
		).save(flush: true)

		subscriptionService.afterIntegrationKeyCreated(integrationKey)
		return integrationKey
	}

	@GrailsCompileStatic
	void delete(String integrationKeyId, SecUser currentUser) {
		IntegrationKey account = IntegrationKey.findByIdAndUser(integrationKeyId, currentUser)
		if (account) {
			subscriptionService.beforeIntegrationKeyRemoved(account)
			account.delete(flush: true)
		}
	}

	String decryptPrivateKey(IntegrationKey key) {
		Map json = JSON.parse(key.json)
		return encryptor.decrypt((String) json.privateKey, key.user.id.byteValue())
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
