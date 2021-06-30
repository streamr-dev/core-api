package com.unifina.controller

import com.unifina.domain.DataUnionJoinRequest
import com.unifina.domain.User
import com.unifina.service.*
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import spock.lang.Specification

@TestFor(DataUnionJoinRequestApiController)
@Mock([RESTAPIFilters, User, DataUnionJoinRequest])
class DataUnionJoinRequestApiControllerSpec extends Specification {
	User me
	final String contractAddress = "0x6c90aece04198da2d5ca9b956b8f95af8041de37"
	final String validID = "L-TvrBkyQTS_JK1ABHFEZAaZ3FHq7-TPqMXe9JNz1x6g"

	def setup() {
		me = new User(id: 1, name: "firstname lastname", username: "firstname.lastname@address.com")
		me.save(validate: true, failOnError: true)
		controller.dataUnionJoinRequestService = Mock(DataUnionJoinRequestService)
		controller.ethereumService = Mock(EthereumService)
	}

	void "isDataUnionAddress"(String value, Object expected) {
		expect:
		DataUnionJoinRequestApiController.isDataUnionAddress(value) == expected
		where:
		value                                        | expected
		"0x0000000000000000000000000000000000000000" | true
		"0x0000000000000000000000000000AAAA0000FFFF" | true
		null                                         | false
		""                                           | false
		"0x1"                                        | false
	}

	void "findAll() test"() {
		setup:
		DataUnionJoinRequest r = new DataUnionJoinRequest(
			user: me,
			memberAddress: "0x0000000000000000000000000000000000000001",
			contractAddress: contractAddress,
		)
		r.id = validID
		r.save(failOnError: true, validate: true)
		when:
		request.apiUser = me
		request.method = "GET"
		params.contractAddress = contractAddress
		params.state = null
		withFilters(action: "index") {
			controller.index()
		}
		then:
		1 * controller.ethereumService.fetchDataUnionAdminsEthereumAddress(contractAddress) >> "adminAddress"
		1 * controller.dataUnionJoinRequestService.findAll(contractAddress, null) >> [r]
		response.json[0].id == validID
		response.json[0].memberAddress == "0x0000000000000000000000000000000000000001"
		response.json[0].contractAddress == contractAddress
		response.json[0].state == "PENDING"
		response.status == 200
	}

	void "findAll() bad request on invalid contract address input"() {
		when:
		request.method = "GET"
		params.contractAddress = "0x123"
		params.state = "APPROVED"
		withFilters(action: "index") {
			controller.index()
		}
		then:
		0 * controller.dataUnionJoinRequestService._
		def e = thrown(BadRequestException)
		e.statusCode == 400
		e.code == "PARAMETER_MISSING"
	}

	void "findAll() checks admin access control"() {
		when:
		request.apiUser = me
		request.method = "GET"
		params.contractAddress = contractAddress
		params.state = "APPROVED"
		withFilters(action: "index") {
			controller.index()
		}
		then:
		1 * controller.ethereumService.fetchDataUnionAdminsEthereumAddress(contractAddress) >> null
		def e = thrown(ApiException)
		e.statusCode == 400
	}

	void "save() test"() {
		setup:
		DataUnionJoinRequest r = new DataUnionJoinRequest(
			memberAddress: "0xCCCC000000000000000000000000AAAA0000FFFF",
			contractAddress: contractAddress,
			user: me,
			state: DataUnionJoinRequest.State.ACCEPTED,
		)
		r.id = validID
		r.save(failOnError: true, validate: true)
		when:
		request.method = "POST"
		request.apiUser = me
		request.json = [
			memberAddress: "0xCCCC000000000000000000000000AAAA0000FFFF",
			secret: "secret",
			metadata: [
				foo: "bar",
			],
		]
		params.contractAddress = contractAddress
		withFilters(action: "save") {
			controller.save()
		}
		then:
		1 * controller.dataUnionJoinRequestService.create(contractAddress, _ as DataUnionJoinRequestCommand, me) >> r
		response.json.id == validID
		response.json.memberAddress == "0xCCCC000000000000000000000000AAAA0000FFFF"
		response.json.contractAddress == contractAddress
		response.json.state == "ACCEPTED"
		response.status == 200
	}

	void "save() bad request when json memberAddress is not an ethereum address"() {
		when:
		request.method = "POST"
		request.json = [
			memberAddress: "0x123",
			secret: "secret",
		]
		params.contractAddress = contractAddress
		withFilters(action: "save") {
			controller.save()
		}
		then:
		0 * controller.dataUnionJoinRequestService._
		def e = thrown(BadRequestException)
		e.statusCode == 400
		e.code == "PARAMETER_MISSING"
	}

	void "save() bad request on invalid contract address input"() {
		when:
		request.method = "GET"
		params.contractAddress = "0x123"
		withFilters(action: "save") {
			controller.save()
		}
		then:
		0 * controller.dataUnionJoinRequestService._
		def e = thrown(BadRequestException)
		e.statusCode == 400
		e.code == "PARAMETER_MISSING"
	}

	void "show() test"() {
		DataUnionJoinRequest r = new DataUnionJoinRequest(
			memberAddress: "0xCCCC000000000000000000000000AAAA0000FFFF",
			contractAddress: contractAddress,
			user: me,
			state: DataUnionJoinRequest.State.ACCEPTED,
		)
		r.id = validID
		r.save(failOnError: true, validate: true)
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
		1 * controller.dataUnionJoinRequestService.find(contractAddress, validID) >> r
		response.json.memberAddress == "0xCCCC000000000000000000000000AAAA0000FFFF"
		response.json.contractAddress == contractAddress
		response.json.id == validID
		response.json.state == "ACCEPTED"
		response.status == 200
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
		0 * controller.dataUnionJoinRequestService._
		def e = thrown(BadRequestException)
		e.statusCode == 400
		e.code == "PARAMETER_MISSING"
	}

	void "show() bad request on invalid join request id"() {
		when:
		request.method = "GET"
		params.contractAddress = contractAddress
		params.id = null
		withFilters(action: "show") {
			controller.show()
		}
		then:
		0 * controller.dataUnionJoinRequestService._
		def e = thrown(BadRequestException)
		e.statusCode == 400
		e.code == "PARAMETER_MISSING"
	}

	void "show() not found 404 on bad DataUnionJoinRequest id"() {
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
		1 * controller.dataUnionJoinRequestService.find(contractAddress, validID) >> null
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
		DataUnionJoinRequest r = new DataUnionJoinRequest(
			memberAddress: "0xCCCC000000000000000000000000AAAA0000FFFF",
			contractAddress: contractAddress,
			user: me,
			state: DataUnionJoinRequest.State.PENDING,
		)
		r.id = validID
		r.save(failOnError: true, validate: true)
		when:
		request.apiUser = me
		request.method = "PUT"
		request.json = [
			state: "ACCEPTED",
		]
		params.contractAddress = contractAddress
		params.id = validID
		withFilters(action: "update") {
			controller.update()
		}
		then:
		1 * controller.ethereumService.fetchDataUnionAdminsEthereumAddress(contractAddress) >> "adminAddress"
		1 * controller.dataUnionJoinRequestService.update(contractAddress, validID, _ as DataUnionUpdateJoinRequestCommand) >> {
			r.state = DataUnionJoinRequest.State.ACCEPTED
			return r
		}
		response.status == 200
		response.json.id == validID
		response.json.state == "ACCEPTED"
		response.json.memberAddress == "0xCCCC000000000000000000000000AAAA0000FFFF"
		response.json.contractAddress == contractAddress
	}

	void "update() bad request on invalid contract address"() {
		when:
		request.method = "PUT"
		request.json = [
			state: "ACCEPTED",
		]
		params.contractAddress = "0x123"
		params.id = validID
		withFilters(action: "update") {
			controller.update()
		}
		then:
		0 * controller.dataUnionJoinRequestService._
		def e = thrown(BadRequestException)
		e.statusCode == 400
		e.code == "PARAMETER_MISSING"
	}

	void "update() bad request when state is not valid"() {
		when:
		request.method = "PUT"
		request.json = [
			state: "ACCEPTXXX",
		]
		params.contractAddress = contractAddress
		params.id = validID
		withFilters(action: "update") {
			controller.update()
		}
		then:
		0 * controller.dataUnionJoinRequestService._
		def e = thrown(BadRequestException)
		e.statusCode == 400
		e.code == "PARAMETER_MISSING"
	}

	void "update() bad request on invalid join request id"() {
		when:
		request.method = "PUT"
		request.json = [
			state: "ACCEPTED",
		]
		params.contractAddress = contractAddress
		params.id = null
		withFilters(action: "update") {
			controller.update()
		}
		then:
		0 * controller.dataUnionJoinRequestService._
		def e = thrown(BadRequestException)
		e.statusCode == 400
		e.code == "PARAMETER_MISSING"
	}

	void "update() not found 404 on bad join request id"() {
		when:
		request.apiUser = me
		request.method = "PUT"
		request.json = [
			state: "ACCEPTED",
		]
		params.contractAddress = contractAddress
		params.id = validID // ID not found in DB
		withFilters(action: "update") {
			controller.update()
		}
		then:
		1 * controller.ethereumService.fetchDataUnionAdminsEthereumAddress(contractAddress) >> "adminAddress"
		1 * controller.dataUnionJoinRequestService.update(contractAddress, validID, _ as DataUnionUpdateJoinRequestCommand) >> {
			throw new NotFoundException("mocked: entity not found")
		}
		def e = thrown(NotFoundException)
		e.statusCode == 404
		e.code == "NOT_FOUND"
	}

	void "update() checks admin access control"() {
		when:
		request.apiUser = me
		request.method = "PUT"
		request.json = [
			state: "ACCEPTED",
		]
		params.contractAddress = contractAddress
		params.id = validID
		withFilters(action: "update") {
			controller.update()
		}
		then:
		1 * controller.ethereumService.fetchDataUnionAdminsEthereumAddress(contractAddress) >> null
		def e = thrown(ApiException)
		e.statusCode == 400
	}

	void "delete() test"() {
		setup:
		DataUnionJoinRequest r = new DataUnionJoinRequest(
			memberAddress: "0xCCCC000000000000000000000000AAAA0000FFFF",
			contractAddress: contractAddress,
			user: me,
			state: DataUnionJoinRequest.State.ACCEPTED,
		)
		r.id = validID
		r.save(failOnError: true, validate: true)
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
		1 * controller.dataUnionJoinRequestService.delete(contractAddress, validID)
		response.status == 204
	}

	void "delete() bad request on invalid contract address"() {
		when:
		request.method = "DELETE"
		params.contractAddress = "0x123"
		params.id = validID
		withFilters(action: "delete") {
			controller.delete()
		}
		then:
		0 * controller.dataUnionJoinRequestService._
		def e = thrown(BadRequestException)
		e.statusCode == 400
		e.code == "PARAMETER_MISSING"
	}

	void "delete() bad request on invalid join request id"() {
		when:
		request.method = "DELETE"
		params.contractAddress = contractAddress
		params.id = "0x123"
		withFilters(action: "delete") {
			controller.delete()
		}
		then:
		0 * controller.dataUnionJoinRequestService._
		def e = thrown(BadRequestException)
		e.statusCode == 400
		e.code == "PARAMETER_MISSING"
	}

	void "delete() not found 404 on bad join request id"() {
		when:
		request.apiUser = me
		request.method = "DELETE"
		params.contractAddress = contractAddress
		params.id = validID // ID not found in DB
		withFilters(action: "delete") {
			controller.delete()
		}
		then:
		1 * controller.ethereumService.fetchDataUnionAdminsEthereumAddress(contractAddress) >> "adminAddress"
		1 * controller.dataUnionJoinRequestService.delete(contractAddress, validID) >> {
			throw new NotFoundException("mocked: entity not found")
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
