package com.unifina.controller

import com.unifina.domain.DataUnionSecret
import com.unifina.domain.User
import com.unifina.service.*
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import spock.lang.Specification

@TestFor(DataUnionSecretApiController)
@Mock([RESTAPIFilters, DataUnionSecret, User])
class DataUnionSecretApiControllerSpec extends Specification {
	User me
	final String contractAddress = "0x6c90aece04198da2d5ca9b956b8f95af8041de37"
	final String validID = "L-TvrBkyQTS_JK1ABHFEZAaZ3FHq7-TPqMXe9JNz1x6g"

	void setup() {
		me = new User(id: 1, name: "firstname lastname", username: "firstname.lastname@address.com")
		me.save(validate: true, failOnError: true)
		DataUnionSecret secret = new DataUnionSecret(
			name: "name of the secret",
			secret: "secret",
			contractAddress: contractAddress,
		)
		secret.id = "secret-id"
		secret.save(validate: true, failOnError: true)

		controller.dataUnionSecretService = Mock(DataUnionSecretService)
		controller.ethereumService = Mock(EthereumService)
	}

	void "index() test"() {
		setup:
		DataUnionSecret s1 = new DataUnionSecret(
			name: "secret 1",
			secret: "secret1",
			contractAddress: contractAddress,
		)
		s1.id = "1"
		s1.save(validate: true, failOnError: true)
		DataUnionSecret s2 = new DataUnionSecret(
			name: "secret 2",
			secret: "secret2",
			contractAddress: contractAddress,
		)
		s2.id = "2"
		s2.save(validate: true, failOnError: true)
		when:
		request.apiUser = me
		request.method = "GET"
		params.contractAddress = contractAddress
		withFilters(action: "index") {
			controller.index()
		}
		then:
		1 * controller.ethereumService.fetchDataUnionAdminsEthereumAddress(contractAddress) >> "adminAddress"
		1 * controller.dataUnionSecretService.findAll(contractAddress) >> [s1, s2]
		response.json[0].id == "1"
		response.json[0].name == "secret 1"
		response.json[0].secret == "secret1"
		response.json[0].contractAddress == contractAddress
		response.json[1].id == "2"
		response.json[1].name == "secret 2"
		response.json[1].secret == "secret2"
		response.json[1].contractAddress == contractAddress
	}

	void "index() bad request on invalid contract address"() {
		when:
		request.method = "GET"
		params.contractAddress = "0x123"
		withFilters(action: "index") {
			controller.index()
		}
		then:
		0 * controller.dataUnionSecretService._
		def e = thrown(BadRequestException)
		e.statusCode == 400
		e.code == "PARAMETER_MISSING"
	}

	void "index() checks admin access control"() {
		when:
		request.apiUser = me
		request.method = "GET"
		params.contractAddress = contractAddress
		withFilters(action: "index") {
			controller.index()
		}
		then:
		1 * controller.ethereumService.fetchDataUnionAdminsEthereumAddress(contractAddress) >> null
		def e = thrown(ApiException)
		e.statusCode == 400
	}

	void "save() test"() {
		DataUnionSecret secret = new DataUnionSecret(
			name: "secret name",
			secret: "secret",
			contractAddress: contractAddress,
		)
		secret.id = "1"
		secret.save(validate: true, failOnError: true)
		when:
		request.apiUser = me
		request.method = "POST"
		request.json = [
			name: "secret name",
			secret: "secret",
		]
		params.contractAddress = contractAddress
		withFilters(action: "save") {
			controller.save()
		}
		then:
		1 * controller.ethereumService.fetchDataUnionAdminsEthereumAddress(contractAddress) >> "adminAddress"
		1 * controller.dataUnionSecretService.create(contractAddress, _ as DataUnionSecretCommand) >> secret
		response.json.id == "1"
		response.json.name == "secret name"
		response.json.secret == "secret"
		response.json.contractAddress == contractAddress
	}

	void "save() bad request on invalid contract address"() {
		when:
		request.method = "POST"
		request.json = [
			name: "secret name",
			secret: "secret",
		]
		params.contractAddress = "0x123"
		withFilters(action: "save") {
			controller.save()
		}
		then:
		0 * controller.dataUnionSecretService._
		def e = thrown(BadRequestException)
		e.statusCode == 400
		e.code == "PARAMETER_MISSING"
	}

	void "save() bad request on non-data union contract address"() {
		when:
		request.method = "POST"
		params.contractAddress = contractAddress
		request.json = [
			name: "secret name",
			secret: "secret",
		]
		withFilters(action: "save") {
			controller.save()
		}
		then:
		1 * controller.ethereumService.fetchDataUnionAdminsEthereumAddress(contractAddress) >> null
		def e = thrown(BadRequestException)
		e.statusCode == 400
		e.code == "PARAMETER_MISSING"
	}

	void "save() bad request on invalid name of secret"() {
		when:
		request.method = "POST"
		request.json = [
			name: "",
		]
		params.contractAddress = contractAddress
		withFilters(action: "save") {
			controller.save()
		}
		then:
		0 * controller.dataUnionSecretService._
		def e = thrown(BadRequestException)
		e.statusCode == 400
		e.code == "PARAMETER_MISSING"
	}

	void "save() checks admin access control"() {
		when:
		request.apiUser = me
		request.method = "POST"
		request.json = [
			name: "secret name",
		]
		params.contractAddress = contractAddress
		withFilters(action: "save") {
			controller.save()
		}
		then:
		1 * controller.ethereumService.fetchDataUnionAdminsEthereumAddress(contractAddress) >> null
		def e = thrown(ApiException)
		e.statusCode == 400
	}

	void "show() test"() {
		setup:
		DataUnionSecret s1 = new DataUnionSecret(
			name: "secret 1",
			secret: "secret1",
			contractAddress: contractAddress,
		)
		s1.id = "1"
		s1.save(validate: true, failOnError: true)
		when:
		request.apiUser = me
		request.method = "GET"
		params.contractAddress = contractAddress
		params.id = validID
		withFilters(action: "show") {
			controller.show()
		}
		then:
		1 * controller.ethereumService.fetchDataUnionAdminsEthereumAddress(contractAddress) >> "adminAddress"
		1 * controller.dataUnionSecretService.find(contractAddress, validID) >> s1
		response.json.id == "1"
		response.json.name == "secret 1"
		response.json.secret == "secret1"
		response.json.contractAddress == contractAddress
	}

	void "show() bad request on invalid contract address"() {
		when:
		request.method = "GET"
		params.contractAddress = "0x123"
		params.id = validID
		withFilters(action: "show") {
			controller.show()
		}
		then:
		0 * controller.dataUnionSecretService._
		def e = thrown(BadRequestException)
		e.statusCode == 400
		e.code == "PARAMETER_MISSING"
	}

	void "show() bad request on non-data union contract address"() {
		when:
		request.method = "GET"
		params.contractAddress = contractAddress
		params.id = validID
		withFilters(action: "show") {
			controller.show()
		}
		then:
		1 * controller.ethereumService.fetchDataUnionAdminsEthereumAddress(contractAddress) >> null
		def e = thrown(BadRequestException)
		e.statusCode == 400
		e.code == "PARAMETER_MISSING"
	}

	void "show() bad request on invalid secret id"() {
		when:
		request.method = "GET"
		params.contractAddress = contractAddress
		params.id = null
		withFilters(action: "show") {
			controller.show()
		}
		then:
		0 * controller.dataUnionSecretService._
		def e = thrown(BadRequestException)
		e.statusCode == 400
		e.code == "PARAMETER_MISSING"
	}

	void "show() not found when secret not found by id"() {
		when:
		request.apiUser = me
		request.method = "GET"
		params.contractAddress = contractAddress
		params.id = validID // ID not found in DB
		withFilters(action: "show") {
			controller.show()
		}
		then:
		1 * controller.ethereumService.fetchDataUnionAdminsEthereumAddress(contractAddress) >> "adminAddress"
		1 * controller.dataUnionSecretService.find(contractAddress, validID) >> null
		def e = thrown(NotFoundException)
		e.statusCode == 404
		e.code == "NOT_FOUND"
	}

	void "show() checks admin access control"() {
		when:
		request.apiUser = me
		request.method = "GET"
		params.contractAddress = contractAddress
		params.id = validID
		withFilters(action: "show") {
			controller.show()
		}
		then:
		1 * controller.ethereumService.fetchDataUnionAdminsEthereumAddress(contractAddress) >> null
		def e = thrown(ApiException)
		e.statusCode == 400
	}

	void "update() test"() {
		setup:
		DataUnionSecret s1 = new DataUnionSecret(
			name: "name",
			secret: "secret 1",
			contractAddress: contractAddress,
		)
		s1.id = "1"
		s1.save(validate: true, failOnError: true)
		when:
		request.apiUser = me
		request.method = "PUT"
		params.contractAddress = contractAddress
		params.id = validID // ID not found in DB
		request.json = [
			name: "new name",
		]
		withFilters(action: "update") {
			controller.update()
		}
		then:
		1 * controller.ethereumService.fetchDataUnionAdminsEthereumAddress(contractAddress) >> "adminAddress"
		1 * controller.dataUnionSecretService.update(contractAddress, validID, _ as DataUnionSecretCommand) >> {
			s1.name = "new name"
			s1.secret = "new secret"
			return s1
		}
		response.status == 200
		response.json.id == "1"
		response.json.name == "new name"
		response.json.secret == "new secret"
	}

	void "update() bad request on invalid contract address"() {
		when:
		request.method = "PUT"
		params.contractAddress = "0x123"
		params.id = "123"
		request.json = [
			name: "name",
		]
		withFilters(action: "update") {
			controller.update()
		}
		then:
		0 * controller.dataUnionSecretService._
		def e = thrown(BadRequestException)
		e.statusCode == 400
		e.code == "PARAMETER_MISSING"
	}

	void "update() bad request on non-data union contract address"() {
		when:
		request.method = "PUT"
		params.contractAddress = contractAddress
		params.id = "123"
		request.json = [
			name: "name",
		]
		withFilters(action: "update") {
			controller.update()
		}
		then:
		1 * controller.ethereumService.fetchDataUnionAdminsEthereumAddress(contractAddress) >> null
		def e = thrown(BadRequestException)
		e.statusCode == 400
		e.code == "PARAMETER_MISSING"
	}

	void "update() bad request on invalid secret id"() {
		when:
		request.method = "PUT"
		params.contractAddress = contractAddress
		params.id = null
		request.json = [
			name: "name",
		]
		withFilters(action: "update") {
			controller.update()
		}
		then:
		0 * controller.dataUnionSecretService._
		def e = thrown(BadRequestException)
		e.statusCode == 400
		e.code == "PARAMETER_MISSING"
	}

	void "update() bad request on invalid secret name"() {
		when:
		request.method = "PUT"
		params.contractAddress = contractAddress
		params.id = validID
		request.json = [
			name: "",
		]
		withFilters(action: "update") {
			controller.update()
		}
		then:
		0 * controller.dataUnionSecretService._
		def e = thrown(BadRequestException)
		e.statusCode == 400
		e.code == "PARAMETER_MISSING"
	}

	void "update() throws NotFoundException when secret not found by id"() {
		when:
		request.apiUser = me
		request.method = "PUT"
		params.contractAddress = contractAddress
		params.id = validID // ID not found in DB
		request.json = [
			name: "name",
		]
		withFilters(action: "update") {
			controller.update()
		}
		then:
		1 * controller.ethereumService.fetchDataUnionAdminsEthereumAddress(contractAddress) >> "adminAddress"
		1 * controller.dataUnionSecretService.update(contractAddress, validID, _ as DataUnionSecretCommand) >> { throw new NotFoundException("Secret not found") }
		def e = thrown(NotFoundException)
		e.statusCode == 404
		e.code == "NOT_FOUND"
	}

	void "update() checks admin access control"() {
		when:
		request.apiUser = me
		request.method = "PUT"
		params.contractAddress = contractAddress
		params.id = validID
		request.json = [
			name: "name",
		]
		withFilters(action: "update") {
			controller.update()
		}
		then:
		1 * controller.ethereumService.fetchDataUnionAdminsEthereumAddress(contractAddress) >> null
		def e = thrown(ApiException)
		e.statusCode == 400
	}

	void "delete() test"() {
		when:
		request.apiUser = me
		request.method = "DELETE"
		params.contractAddress = contractAddress
		params.id = validID
		withFilters(action: "delete") {
			controller.delete()
		}
		then:
		1 * controller.ethereumService.fetchDataUnionAdminsEthereumAddress(contractAddress) >> "adminAddress"
		1 * controller.dataUnionSecretService.delete(contractAddress, validID)
		response.status == 204
	}

	void "delete() throws on invalid contract address"() {
		when:
		request.method = "DELETE"
		params.contractAddress = "0x123"
		params.id = validID
		withFilters(action: "delete") {
			controller.delete()
		}
		then:
		0 * controller.dataUnionSecretService._
		def e = thrown(BadRequestException)
		e.statusCode == 400
		e.code == "PARAMETER_MISSING"
	}

	void "delete() bad request on non-data union contract address"() {
		when:
		request.method = "DELETE"
		params.contractAddress = contractAddress
		params.id = validID
		withFilters(action: "delete") {
			controller.delete()
		}
		then:
		1 * controller.ethereumService.fetchDataUnionAdminsEthereumAddress(contractAddress) >> null
		def e = thrown(BadRequestException)
		e.statusCode == 400
		e.code == "PARAMETER_MISSING"
	}

	void "delete() bad request on invalid secret id"() {
		when:
		request.method = "DELETE"
		params.contractAddress = contractAddress
		params.id = null
		withFilters(action: "delete") {
			controller.delete()
		}
		then:
		0 * controller.dataUnionSecretService._
		def e = thrown(BadRequestException)
		e.statusCode == 400
		e.code == "PARAMETER_MISSING"
	}

	void "delete() not found on non-existing secret id"() {
		when:
		request.apiUser = me
		request.method = "DELETE"
		params.contractAddress = contractAddress
		params.id = validID
		withFilters(action: "delete") {
			controller.delete()
		}
		then:
		1 * controller.ethereumService.fetchDataUnionAdminsEthereumAddress(contractAddress) >> "adminAddress"
		1 * controller.dataUnionSecretService.delete(contractAddress, validID) >> {
			throw new NotFoundException("mocked: not found!")
		}
		def e = thrown(NotFoundException)
		e.statusCode == 404
		e.code == "NOT_FOUND"
	}

	void "delete() checks admin access control"() {
		when:
		request.apiUser = me
		request.method = "DELETE"
		params.contractAddress = contractAddress
		params.id = validID
		withFilters(action: "delete") {
			controller.delete()
		}
		then:
		1 * controller.ethereumService.fetchDataUnionAdminsEthereumAddress(contractAddress) >> null
		def e = thrown(ApiException)
		e.statusCode == 400
	}
}
