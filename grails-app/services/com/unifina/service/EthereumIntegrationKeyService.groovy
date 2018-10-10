package com.unifina.service

import com.lambdaworks.redis.RedisClient
import com.lambdaworks.redis.RedisConnection
import com.lambdaworks.redis.RedisURI
import com.unifina.api.ApiException
import com.unifina.api.DuplicateNotAllowedException
import com.unifina.crypto.ECRecover
import com.unifina.domain.security.IntegrationKey
import com.unifina.domain.security.SecUser
import com.unifina.security.StringEncryptor
import com.unifina.utils.MapTraversal
import grails.compiler.GrailsCompileStatic
import grails.converters.JSON
import grails.util.Holders
import groovy.transform.CompileStatic
import org.apache.commons.codec.DecoderException
import org.apache.commons.codec.binary.Hex
import org.apache.commons.lang.RandomStringUtils
import org.ethereum.crypto.ECKey
import org.springframework.util.Assert

import javax.annotation.PostConstruct
import java.security.SignatureException

class EthereumIntegrationKeyService {

	def grailsApplication
	StringEncryptor encryptor
	SubscriptionService subscriptionService
	ChallengeService challengeService

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
		String address
		try {
			address = challengeService.verifyChallengeAndGetAddress(challengeID, challenge, signature)
			if(address==null) {
				throw new ApiException(400, "INVALID_CHALLENGE", "challenge validation failed")
			}
		} catch (SignatureException | DecoderException e) {
			throw new ApiException(400, "ADDRESS_RECOVERY_ERROR", e.message)
		}

		if (IntegrationKey.findByServiceAndIdInService(IntegrationKey.Service.ETHEREUM_ID, address) != null) {
			throw new DuplicateNotAllowedException("This Ethereum address is already associated with another Streamr user.")
		}

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
			if (account.service == IntegrationKey.Service.ETHEREUM_ID) {
				subscriptionService.beforeIntegrationKeyRemoved(account)
			}
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

	SecUser getOrCreateFromEthereumAddress(String address) {
		IntegrationKey key = IntegrationKey.findByIdInServiceAndService(address, IntegrationKey.Service.ETHEREUM_ID)
		if (key == null) {
			Calendar now = Calendar.getInstance()
			TimeZone timeZone = now.getTimeZone()
			SecUser user = new SecUser(
				username: address,
				password: generatePassword(16),
				name: address,
				timezone: timeZone.getDisplayName()
			).save(failOnError: true, flush: true, validate: true)
			key = new IntegrationKey(
				name: address,
				user: user,
				service: IntegrationKey.Service.ETHEREUM_ID,
				idInService: address,
				json: ([
					address: new String(address)
				] as JSON).toString()
			).save(failOnError: true, flush: true)
		}
		return key.user
	}

	//TODO: Must be moved to some util class. Maybe in java.com.unifina.security.SomeClass?
	private String generatePassword(int length) {
		String charset = (('a'..'z') + ('A'..'Z') + ('0'..'9')).join()
		return RandomStringUtils.random(length, charset.toCharArray())
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
