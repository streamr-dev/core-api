package com.unifina.service

import com.unifina.api.BadRequestException
import com.unifina.api.DataUnionSecretCommand
import com.unifina.api.NotFoundException
import com.unifina.domain.dataunion.DataUnionSecret
import com.unifina.utils.IdGenerator
import spock.lang.Specification

class DataUnionSecretServiceIntegrationSpec extends Specification {
	DataUnionSecretService service = new DataUnionSecretService()
	final String contractAddress = "0x0000000000000000000000000000000000000000"

	void "findAll() test"() {
		setup:
		DataUnionSecret s1 = new DataUnionSecret(
			name: "secret 1",
			secret: "secret#1",
			contractAddress: contractAddress,
		)
		s1.save(validate: true, failOnError: true)
		DataUnionSecret s2 = new DataUnionSecret(
			name: "secret 2",
			secret: "secret#2",
			contractAddress: contractAddress,
		)
		s2.save(validate: true, failOnError: true)
		DataUnionSecret s3 = new DataUnionSecret(
			name: "secret 3",
			secret: "secret#3",
			contractAddress: "0xCCCC000000000000000000000000AAAA0000FFFF",
		)
		s3.save(validate: true, failOnError: true)
		when:
		List<DataUnionSecret> results = service.findAll(contractAddress)
		then:
		results.size() == 2
		results.containsAll([s1, s2])
	}

	void "create() test with generated secret"() {
		setup:
		service.generator = Mock(IdGenerator)
		DataUnionSecretCommand cmd = new DataUnionSecretCommand(
			name: "name of secret",
		)
		when:
		DataUnionSecret result = service.create(contractAddress, cmd)
		then:
		1 * service.generator.generate() >> "secret"
		result.id != null
		result.name == "name of secret"
		result.contractAddress == contractAddress
		result.secret == "secret"
	}

	void "create() test with given secret"() {
		setup:
		service.generator = Mock(IdGenerator)
		DataUnionSecretCommand cmd = new DataUnionSecretCommand(
			name: "name of secret",
			secret: "mySecret"
		)
		when:
		DataUnionSecret result = service.create(contractAddress, cmd)
		then:
		0 * service.generator.generate()
		result.id != null
		result.name == "name of secret"
		result.contractAddress == contractAddress
		result.secret == "mySecret"
	}

	void "find() test"() {
		setup:
		DataUnionSecret s1 = new DataUnionSecret(
			name: "secret 1",
			secret: "secret#1",
			contractAddress: contractAddress,
		)
		s1.save(validate: true, failOnError: true)
		DataUnionSecret s2 = new DataUnionSecret(
			name: "secret 2",
			secret: "secret#2",
			contractAddress: contractAddress,
		)
		s2.save(validate: true, failOnError: true)
		DataUnionSecret s3 = new DataUnionSecret(
			name: "secret 3",
			secret: "secret#3",
			contractAddress: "0xCCCC000000000000000000000000AAAA0000FFFF",
		)
		s3.save(validate: true, failOnError: true)
		when:
		DataUnionSecret result = service.find(contractAddress, s1.id)
		then:
		result.name == "secret 1"
		result.secret == "secret#1"
		result.contractAddress == contractAddress
	}

	void "update() only name"() {
		setup:
		DataUnionSecret s1 = new DataUnionSecret(
			name: "secret 1",
			secret: "secret#1",
			contractAddress: contractAddress,
		)
		s1.save(validate: true, failOnError: true)
		DataUnionSecretCommand cmd = new DataUnionSecretCommand(
			name: "new secret name",
		)
		when:
		DataUnionSecret result = service.update(contractAddress, s1.id, cmd)
		then:
		result.name == "new secret name"
		result.secret == "secret#1"
		result.contractAddress == contractAddress
	}

	void "update() only secret"() {
		setup:
		DataUnionSecret s1 = new DataUnionSecret(
			name: "secret 1",
			secret: "secret#1",
			contractAddress: contractAddress,
		)
		s1.save(validate: true, failOnError: true)
		DataUnionSecretCommand cmd = new DataUnionSecretCommand(
			secret: "new secret",
		)
		when:
		DataUnionSecret result = service.update(contractAddress, s1.id, cmd)
		then:
		thrown BadRequestException
	}

	void "update() both name and secret"() {
		setup:
		DataUnionSecret s1 = new DataUnionSecret(
			name: "secret 1",
			secret: "secret#1",
			contractAddress: contractAddress,
		)
		s1.save(validate: true, failOnError: true)
		DataUnionSecretCommand cmd = new DataUnionSecretCommand(
			name: "new secret name",
			secret: "new secret",
		)
		when:
		DataUnionSecret result = service.update(contractAddress, s1.id, cmd)
		then:
		thrown BadRequestException
	}

	void "update() fails for bad id"() {
		setup:
		DataUnionSecret s1 = new DataUnionSecret(
			name: "secret 1",
			secret: "secret#1",
			contractAddress: contractAddress,
		)
		s1.save(validate: true, failOnError: true)
		DataUnionSecretCommand cmd = new DataUnionSecretCommand(
			name: "new secret name",
			secret: "new secret",
		)
		when:
		DataUnionSecret result = service.update(contractAddress, "invalid id", cmd)
		then:
		thrown NotFoundException
	}

	void "delete() test"() {
		setup:
		DataUnionSecret s1 = new DataUnionSecret(
			name: "secret 1",
			secret: "secret#1",
			contractAddress: contractAddress,
		)
		s1.save(validate: true, failOnError: true)
		when:
		service.delete(contractAddress, s1.id)
		then:
		DataUnionSecret.findById(s1.id) == null
	}
}
