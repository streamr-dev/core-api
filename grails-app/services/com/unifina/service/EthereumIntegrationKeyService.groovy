package com.unifina.service

import com.unifina.api.ApiException
import com.unifina.api.CannotRemoveEthereumKeyException
import com.unifina.api.ChallengeVerificationFailedException
import com.unifina.api.DuplicateNotAllowedException
import com.unifina.api.NotFoundException
import com.unifina.domain.data.Feed
import com.unifina.domain.data.Stream
import com.unifina.domain.security.IntegrationKey
import com.unifina.domain.security.SecUser
import com.unifina.security.StringEncryptor
import grails.compiler.GrailsCompileStatic
import grails.converters.JSON
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
	UserService userService

	@PostConstruct
	void init() {
		String password = grailsApplication.config["streamr"]["encryption"]["password"]
		Assert.notNull(password, "streamr.encryption.password not set!")
		encryptor = new StringEncryptor(password)
	}

	IntegrationKey createEthereumAccount(SecUser user, String name, String privateKey) {
		privateKey = trimPrivateKey(privateKey)
		validatePrivateKey(privateKey)

		try {
			String address = "0x" + getAddress(privateKey)
			String encryptedPrivateKey = encryptor.encrypt(privateKey, user.id.byteValue())
			IntegrationKey key = new IntegrationKey(
				name: name,
				user: user,
				service: IntegrationKey.Service.ETHEREUM,
				idInService: address,
				json: ([
					privateKey: encryptedPrivateKey,
					address   : address
				] as JSON).toString()
			).save(flush: true, failOnError: true)

			createUserInboxStream(address)

			subscriptionService.afterIntegrationKeyCreated(key)
			return key
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("Private key must be a valid hex string!")
		}
	}

	IntegrationKey createEthereumID(SecUser user, String name, String challengeID, String challenge, String signature) {
		String address
		try {
			address = challengeService.verifyChallengeAndGetAddress(challengeID, challenge, signature)
		} catch (ChallengeVerificationFailedException e) {
			throw e
		} catch (SignatureException | DecoderException e) {
			throw new ApiException(400, "ADDRESS_RECOVERY_ERROR", e.message)
		}

		if (getEthereumUser(address) != null) {
			throw new DuplicateNotAllowedException("This Ethereum address is already associated with another Streamr user.")
		}

		IntegrationKey integrationKey = new IntegrationKey(
			name: name,
			user: user,
			service: IntegrationKey.Service.ETHEREUM_ID.toString(),
			idInService: address,
			json: ([
				address: address
			] as JSON).toString()
		).save(flush: true)

		createUserInboxStream(address)

		subscriptionService.afterIntegrationKeyCreated(integrationKey)
		return integrationKey
	}

	@GrailsCompileStatic
	void delete(String integrationKeyId, SecUser currentUser) {
		if (currentUser.isEthereumUser()) {
			int nbKeys = IntegrationKey.countByUserAndService(currentUser, IntegrationKey.Service.ETHEREUM_ID)
			if (nbKeys <= 1) {
				throw new CannotRemoveEthereumKeyException("Cannot remove only Ethereum key.")
			}
		}

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

	List<IntegrationKey> getAllPrivateKeysForUser(SecUser user) {
		IntegrationKey.findAllByServiceAndUser(IntegrationKey.Service.ETHEREUM, user)
	}

	SecUser getEthereumUser(String address) {
		IntegrationKey key = IntegrationKey.find {
			idInService == address && service in [IntegrationKey.Service.ETHEREUM, IntegrationKey.Service.ETHEREUM_ID]
		}
		if (key == null) {
			return null
		}
		return key.user
	}

	SecUser getOrCreateFromEthereumAddress(String address) {
		SecUser user = getEthereumUser(address)
		if (user == null) {
			user = createEthereumUser(address)
		}
		return user
	}

	SecUser createEthereumUser(String address) {
		SecUser user = userService.createUser([
			username       : address,
			password       : RandomStringUtils.random(32),
			name           : address,
			enabled        : true,
			accountLocked  : false,
			passwordExpired: false
		])
		new IntegrationKey(
			name: address,
			user: user,
			service: IntegrationKey.Service.ETHEREUM_ID,
			idInService: address,
			json: ([
				address: address
			] as JSON).toString()
		).save(failOnError: true, flush: true)
		createUserInboxStream(address)
		return user
	}

	@CompileStatic
	private static void createUserInboxStream(String address) {
		Stream inboxStream = new Stream()
		inboxStream.id = address
		inboxStream.name = address
		inboxStream.inbox = true
		inboxStream.autoConfigure = false
		// If no feed given, API feed is used
		if (inboxStream.feed == null) {
			inboxStream.feed = Feed.load(Feed.KAFKA_ID)
		}
		inboxStream.save(failOnError: true, flush: true)
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
	private static String getAddress(String privateKey) {
		BigInteger pk = new BigInteger(privateKey, 16)
		ECKey key = ECKey.fromPrivate(pk)
		String publicKey = Hex.encodeHexString(key.getAddress())

		return publicKey
	}

	@CompileStatic
	private static void validatePrivateKey(String privateKey) {
		if (privateKey.length() != 64) { // must be 256 bits long
			throw new IllegalArgumentException("The private key must be a hex string of 64 chars (without the 0x prefix).")
		}
	}

	void updateKey(SecUser user, String id, String name) {
		IntegrationKey key = IntegrationKey.findByIdAndUser(id, user)
		if (key == null) {
			throw new NotFoundException("integration key not found", "IntegrationKey", id)
		}
		key.name = name
		key.save(failOnError: true, validate: true)
	}
}
