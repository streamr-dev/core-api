package com.unifina.service

import com.unifina.api.CommunitySecretCommand
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

	void "create() test"() {
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

	void "update() test"() {
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

	void "deleteCommunitySecret() test"() {
		setup:
		CommunitySecret s1 = new CommunitySecret(
			name: "secret 1",
			secret: "secret#1",
			communityAddress: communityAddress,
		)
		s1.save(validate: true, failOnError: true)
		when:
		service.deleteCommunitySecret(communityAddress, s1.id)
		then:
		CommunitySecret.findById(s1.id) == null
	}
}
