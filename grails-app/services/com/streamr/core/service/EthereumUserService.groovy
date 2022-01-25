package com.streamr.core.service

import com.streamr.core.domain.SignupMethod
import com.streamr.core.domain.User
import groovy.transform.CompileStatic
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.springframework.validation.FieldError
import org.web3j.crypto.Credentials
import org.web3j.crypto.ECKeyPair
import org.web3j.crypto.Keys

class EthereumUserService {
	GrailsApplication grailsApplication

	//
	// Old UserService start
	//

	User createUser(String address, SignupMethod signupMethod) {
		assertUnique(address)
		User user = new User()
		user.username = address
		user.name = "Anonymous User"
		user.enabled = true
		user.accountLocked = false
		user.signupMethod = signupMethod

		if (!user.validate()) {
			def errors = checkErrors(user.errors.getAllErrors())
			def errorStrings = errors.collect { FieldError e ->
				if (e.getCode() == "unique") {
					return "Address already in use."
				} else {
					return e.toString()
				}
			}
			throw new UserCreationFailedException("Registration failed:\n" + errorStrings.join(",\n"))
		}

		if (!user.save(flush: false)) {
			log.warn("Failed to save user data: " + checkErrors(user.errors.getAllErrors()))
			throw new UserCreationFailedException()
		}
		log.info("Created user for " + user.username)

		return user
	}

	def delete(User user) {
		if (user == null) {
			throw new NotFoundException("user not found", "User", null)
		}
		user.enabled = false
		user.save(validate: true)
	}

	/**
	 * Checks if the errors list contains any fields whose values may not be logged
	 * as plaintext (passwords etc.). The excluded fields are read from
	 * grails.exceptionresolver.params.exclude config key.
	 *
	 * If any excluded fields are found, their field values are replaced with "***".
	 * @param errorList
	 * @return
	 */
	List<FieldError> checkErrors(List<FieldError> errorList) {
		List<String> blackList = (List<String>) grailsApplication?.config?.grails?.exceptionresolver?.params?.exclude
		if (blackList == null) {
			blackList = Collections.emptyList()
		}
		List<FieldError> finalErrors = new ArrayList<>()
		List<FieldError> toBeCensoredList = new ArrayList<>()
		errorList.each {
			if (blackList.contains(it.getField())) {
				toBeCensoredList.add(it)
			} else {
				finalErrors.add(it)
			}
		}
		toBeCensoredList.each {
			List arguments = Arrays.asList(it.getArguments())
			int index = arguments.indexOf(it.getRejectedValue())
			if (index >= 0 && index < arguments.size()) {
				arguments.set(index, "***")
			}
			FieldError fieldError = new FieldError(
				it.getObjectName(), it.getField(), "***", it.isBindingFailure(),
				it.getCodes(), arguments.toArray(), it.getDefaultMessage()
			)
			finalErrors.add(fieldError)
		}
		return finalErrors
	}
	//
	// Old UserService ends
	//

	User getEthereumUser(String address) {
		if (address == null) {
			return null
		}
		User user = User.createCriteria().get {
			eq("username", address) // MySQL is set up in case sensitive mode
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
		User user = createUser(address, signupMethod)
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
