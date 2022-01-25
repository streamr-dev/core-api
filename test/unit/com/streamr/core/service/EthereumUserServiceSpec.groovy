package com.streamr.core.service

import com.streamr.core.domain.Role
import com.streamr.core.domain.SignupMethod
import com.streamr.core.domain.User
import com.streamr.core.domain.UserRole
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import org.springframework.validation.FieldError
import org.web3j.crypto.ECKeyPair
import spock.lang.Specification

@TestFor(EthereumUserService)
@Mock([User, UserRole, Role])
class EthereumUserServiceSpec extends Specification {
	String address = "0x8eEEF384734a8cEfeC53eA49eb651D0257cbA6B6"
	User me

    ChallengeService challengeService
    SubscriptionService subscriptionService

	void setup() {
		me = new User(username: address).save(failOnError: true, validate: false)
	}

	void "get address from private key"() {
		setup:
		ECKeyPair keyPair = ECKeyPair.create("0x0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF".getBytes())
		expect:
        EthereumUserService.getAddress(keyPair.privateKey.toString()) == "37aa29f21ccb6a280830ccbefdbb40b9f5b08b34"
	}

	void "create user checks for duplicate address"() {
		when:
		service.createEthereumUser(address, SignupMethod.UNKNOWN)
		then:
		thrown(DuplicateNotAllowedException)
	}

	void "getOrCreateFromEthereumAddress() creates user if key does not exists"() {
		User someoneElse = new User(username: "0x7328Ac6F6ce7442Baa695dB8f1Fc442a01eA3056").save(failOnError: true, validate: false)
		when:
		service.getOrCreateFromEthereumAddress(address, SignupMethod.UNKNOWN)
		then:
		User.count == 2
	}

	void "getOrCreateFromEthereumAddress() returns user if key exists"() {
		when:
		User user = service.getOrCreateFromEthereumAddress(address, SignupMethod.UNKNOWN)
		then:
		user.username == me.username
		User.count == 1
	}

	// Old UserService specs =>

	def "the user is created when called"() {
		setup:
		["ROLE_USER", "ROLE_LIVE", "ROLE_ADMIN"].each { String authority ->
			Role role = new Role()
			role.authority = authority
			role.save()
		}
		String userAddress = "0x000000000000000000ffff0000ddd0000abc1231"

		when:
		User user = service.createUser(userAddress, SignupMethod.CORE)

		then:
		User.findByUsername(userAddress) != null
		user.getAuthorities().size() == 0 // By default, user's have no roles
	}

	void "censoring errors with checkErrors() works properly"() {
		List checkedErrors

		when: "given list of fieldErrors"
		List<FieldError> errorList = new ArrayList<>()
		errorList.add(new FieldError(
			this.getClass().name, 'password', 'rejectedPassword', false, null, ['null', 'null', 'rejectedPassword'].toArray(), null
		))
		errorList.add(new FieldError(
			this.getClass().name, 'username', 'rejectedUsername', false, null, ['null', 'null', 'rejectedUsername'].toArray(), null
		))
		checkedErrors = service.checkErrors(errorList)

		then: "the rejected password is hidden but the rejected username is not"
		checkedErrors.get(0).getField() == "username"
		checkedErrors.get(0).getRejectedValue() == "rejectedUsername"
		checkedErrors.get(0).getArguments() == ['null', 'null', 'rejectedUsername']

		checkedErrors.get(1).getField() == "password"
		checkedErrors.get(1).getRejectedValue() == "***"
		checkedErrors.get(1).getArguments() == ['null', 'null', '***']
	}

	def "delete user"() {
		setup:
		User user = new User()
		user.id = 1

		when:
		service.delete(user)

		then:
		user.enabled == false
	}

	def "delete user validates parameters"() {
		when:
		service.delete(null)

		then:
		thrown NotFoundException
	}
}
