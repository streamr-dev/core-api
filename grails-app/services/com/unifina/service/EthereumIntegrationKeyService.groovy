package com.unifina.service

import com.unifina.domain.SignupMethod
import com.unifina.domain.User
import groovy.transform.CompileStatic
import org.web3j.crypto.Credentials
import org.web3j.crypto.ECKeyPair
import org.web3j.crypto.Keys

class EthereumIntegrationKeyService {
	def grailsApplication
	UserService userService
	PermissionService permissionService

	User getEthereumUser(String address) {
		if (address == null) {
			return null
		}
		User user = User.createCriteria().get {
			// ilike = case-insensitive like: Ethereum addresses are case-insensitive but different case systems
			// are in use (checksum-case, lower-case at least)
			ilike("username", address)
		}
		return user
	}

	User getOrCreateFromEthereumAddress(String address, SignupMethod signupMethod) {
		User user = getEthereumUser(address)
		if (user == null) {
			user = createEthereumUser(address, signupMethod)
		}
		return user
	}

	User createEthereumUser(String address, SignupMethod signupMethod) {
		assertUnique(address)
		User user = userService.createUser([
			username: address,
			name: "Anonymous User",
			enabled: true,
			accountLocked: false,
			signupMethod: signupMethod
		])
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
}
