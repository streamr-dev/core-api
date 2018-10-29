package com.unifina.service

import com.unifina.api.ApiException
import com.unifina.api.CannotRemoveEthereumKeyException
import com.unifina.api.ChallengeVerificationFailedException
import com.unifina.api.DuplicateNotAllowedException
import com.unifina.domain.security.IntegrationKey
import com.unifina.domain.security.SecUser
import com.unifina.security.StringEncryptor
import com.unifina.utils.EthereumAddressValidator
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
		} catch (ChallengeVerificationFailedException e) {
			throw e
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
				address: address
			] as JSON).toString()
		).save(flush: true)

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
			return createEthereumUser(address)
		}
		return key.user
	}

	SecUser createEthereumUser(String address) {
		SecUser user = userService.createUser([
			username       : address,
			password       : RandomStringUtils.random(32),
			name           : address,
			timezone       : "UTC",
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
		return user
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
