package com.unifina.service

import com.unifina.api.BadRequestException
import com.unifina.api.CommunitySecretCommand
import com.unifina.api.NotFoundException
import com.unifina.domain.community.CommunitySecret
import com.unifina.utils.IdGenerator
import spock.lang.Specification

class CommunitySecretServiceIntegrationSpec extends Specification {
	CommunitySecretService service = new CommunitySecretService()
	final String communityAddress = "0x0000000000000000000000000000000000000000"

	void "findAll() test"() {
		setup:
		CommunitySecret s1 = new CommunitySecret(
			name: "secret 1",
			secret: "secret#1",
			communityAddress: communityAddress,
		)
		s1.save(validate: true, failOnError: true)
		CommunitySecret s2 = new CommunitySecret(
			name: "secret 2",
			secret: "secret#2",
			communityAddress: communityAddress,
		)
		s2.save(validate: true, failOnError: true)
		CommunitySecret s3 = new CommunitySecret(
			name: "secret 3",
			secret: "secret#3",
			communityAddress: "0xCCCC000000000000000000000000AAAA0000FFFF",
		)
		s3.save(validate: true, failOnError: true)
		when:
		List<CommunitySecret> results = service.findAll(communityAddress)
		then:
		results.size() == 2
		results.containsAll([s1, s2])
	}

	void "create() test with generated secret"() {
		setup:
		service.generator = Mock(IdGenerator)
		CommunitySecretCommand cmd = new CommunitySecretCommand(
			name: "community secret",
		)
		when:
		CommunitySecret result = service.create(communityAddress, cmd)
		then:
		1 * service.generator.generate() >> "secret"
		result.id != null
		result.name == "community secret"
		result.communityAddress == communityAddress
		result.secret == "secret"
	}

	void "create() test with given secret"() {
		setup:
		service.generator = Mock(IdGenerator)
		CommunitySecretCommand cmd = new CommunitySecretCommand(
			name: "community secret",
			secret: "mySecret"
		)
		when:
		CommunitySecret result = service.create(communityAddress, cmd)
		then:
		0 * service.generator.generate()
		result.id != null
		result.name == "community secret"
		result.communityAddress == communityAddress
		result.secret == "mySecret"
	}

	void "find() test"() {
		setup:
		CommunitySecret s1 = new CommunitySecret(
			name: "secret 1",
			secret: "secret#1",
			communityAddress: communityAddress,
		)
		s1.save(validate: true, failOnError: true)
		CommunitySecret s2 = new CommunitySecret(
			name: "secret 2",
			secret: "secret#2",
			communityAddress: communityAddress,
		)
		s2.save(validate: true, failOnError: true)
		CommunitySecret s3 = new CommunitySecret(
			name: "secret 3",
			secret: "secret#3",
			communityAddress: "0xCCCC000000000000000000000000AAAA0000FFFF",
		)
		s3.save(validate: true, failOnError: true)
		when:
		CommunitySecret result = service.find(communityAddress, s1.id)
		then:
		result.name == "secret 1"
		result.secret == "secret#1"
		result.communityAddress == communityAddress
	}

	void "update() only name"() {
		setup:
		CommunitySecret s1 = new CommunitySecret(
			name: "secret 1",
			secret: "secret#1",
			communityAddress: communityAddress,
		)
		s1.save(validate: true, failOnError: true)
		CommunitySecretCommand cmd = new CommunitySecretCommand(
			name: "new secret name",
		)
		when:
		CommunitySecret result = service.update(communityAddress, s1.id, cmd)
		then:
		result.name == "new secret name"
		result.secret == "secret#1"
		result.communityAddress == communityAddress
	}

	void "update() only secret"() {
		setup:
		CommunitySecret s1 = new CommunitySecret(
			name: "secret 1",
			secret: "secret#1",
			communityAddress: communityAddress,
		)
		s1.save(validate: true, failOnError: true)
		CommunitySecretCommand cmd = new CommunitySecretCommand(
			secret: "new secret",
		)
		when:
		CommunitySecret result = service.update(communityAddress, s1.id, cmd)
		then:
		thrown BadRequestException
	}

	void "update() both name and secret"() {
		setup:
		CommunitySecret s1 = new CommunitySecret(
			name: "secret 1",
			secret: "secret#1",
			communityAddress: communityAddress,
		)
		s1.save(validate: true, failOnError: true)
		CommunitySecretCommand cmd = new CommunitySecretCommand(
			name: "new secret name",
			secret: "new secret",
		)
		when:
		CommunitySecret result = service.update(communityAddress, s1.id, cmd)
		then:
		thrown BadRequestException
	}

	void "update() fails for bad id"() {
		setup:
		CommunitySecret s1 = new CommunitySecret(
			name: "secret 1",
			secret: "secret#1",
			communityAddress: communityAddress,
		)
		s1.save(validate: true, failOnError: true)
		CommunitySecretCommand cmd = new CommunitySecretCommand(
			name: "new secret name",
			secret: "new secret",
		)
		when:
		CommunitySecret result = service.update(communityAddress, "invalid id", cmd)
		then:
		thrown NotFoundException
	}

	void "delete() test"() {
		setup:
		CommunitySecret s1 = new CommunitySecret(
			name: "secret 1",
			secret: "secret#1",
			communityAddress: communityAddress,
		)
		s1.save(validate: true, failOnError: true)
		when:
		service.delete(communityAddress, s1.id)
		then:
		CommunitySecret.findById(s1.id) == null
	}
}
