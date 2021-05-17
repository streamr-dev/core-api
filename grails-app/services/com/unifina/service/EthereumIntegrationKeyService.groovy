package com.unifina.service

import com.unifina.domain.IntegrationKey
import com.unifina.domain.SignupMethod
import com.unifina.domain.User
import grails.compiler.GrailsCompileStatic
import grails.converters.JSON
import groovy.transform.CompileStatic
import org.web3j.crypto.Credentials
import org.web3j.crypto.ECKeyPair
import org.web3j.crypto.Keys

class EthereumIntegrationKeyService {

	def grailsApplication
	SubscriptionService subscriptionService
	ChallengeService challengeService
	UserService userService
	PermissionService permissionService

	IntegrationKey createEthereumID(User user, String name, String challengeID, String challenge, String signature) {
		String address
		try {
			address = challengeService.verifyChallengeAndGetAddress(challengeID, challenge, signature)
		} catch (ChallengeVerificationFailedException e) {
			throw e
		}

		assertUnique(address)

		IntegrationKey integrationKey = new IntegrationKey(
			name: name,
			user: user,
			service: IntegrationKey.Service.ETHEREUM_ID.toString(),
			idInService: address,
			json: ([
				address: address
			] as JSON).toString()
		).save(flush: false)

		subscriptionService.afterIntegrationKeyCreated(integrationKey)
		return integrationKey
	}

	@GrailsCompileStatic
	void delete(String integrationKeyId, User currentUser) {
		int nbKeys = IntegrationKey.countByUserAndService(currentUser, IntegrationKey.Service.ETHEREUM_ID)
		if (nbKeys <= 1) {
			throw new CannotRemoveEthereumKeyException("Cannot remove only Ethereum key.")
		}

		IntegrationKey account = IntegrationKey.findByIdAndUser(integrationKeyId, currentUser)
		if (account) {
			subscriptionService.beforeIntegrationKeyRemoved(account)
			account.delete(flush: true)
		}
	}

	User getEthereumUser(String address) {
		if (address == null) {
			return null
		}
		IntegrationKey key = IntegrationKey.createCriteria().get {
			'in'("service", [IntegrationKey.Service.ETHEREUM_ID])
			// ilike = case-insensitive like: Ethereum addresses are case-insensitive but different case systems
			// are in use (checksum-case, lower-case at least)
			ilike("idInService", address)
		}
		if (key == null) {
			return null
		}
		return key.user
	}

	User getOrCreateFromEthereumAddress(String address, SignupMethod signupMethod) {
		User user = getEthereumUser(address)
		if (user == null) {
			user = createEthereumUser(address, signupMethod)
		}
		return user
	}

	User createEthereumUser(String address, SignupMethod signupMethod) {
		User user = userService.createUser([
			username: address,
			name: "Anonymous User",
			enabled: true,
			accountLocked: false,
			signupMethod: signupMethod
		])
		new IntegrationKey(
			name: address,
			user: user,
			service: IntegrationKey.Service.ETHEREUM_ID,
			idInService: address,
			json: ([
				address: address
			] as JSON).toString()
		).save(failOnError: true, flush: false)
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
	public static String getAddress(String privateKey) {
		BigInteger pk = new BigInteger(privateKey, 16)
		ECKeyPair keyPair = ECKeyPair.create(pk)
		return Keys.getAddress(keyPair.publicKey)
	}

	@CompileStatic
	private static void validatePrivateKey(String privateKey) {
		if (privateKey.length() != 64) { // must be 256 bits long
			throw new IllegalArgumentException("The private key must be a hex string of 64 chars (without the 0x prefix).")
		}
	}

	@CompileStatic
	static Credentials generateAccount() {
		return Credentials.create(Keys.createEcKeyPair())
	}

	private void assertUnique(String address) {
		User existingUser = getEthereumUser(address)
		if (existingUser != null) {
			log.error("The Ethereum address " + address + " is already associated with the Streamr user: " + existingUser.id)
			throw new DuplicateNotAllowedException("The Ethereum address " + address + " is already associated with a Streamr user.")
		}
	}

	void updateKey(User user, String id, String name) {
		IntegrationKey key = IntegrationKey.findByIdAndUser(id, user)
		if (key == null) {
			throw new NotFoundException("integration key not found", "IntegrationKey", id)
		}
		key.name = name
		key.save(failOnError: true, validate: true)
	}

}
