package com.unifina.controller.api

import com.unifina.api.BadRequestException
import com.unifina.api.CommunityJoinRequestCommand
import com.unifina.api.NotFoundException
import com.unifina.api.UpdateCommunityJoinRequestCommand
import com.unifina.domain.community.CommunityJoinRequest
import com.unifina.domain.security.SecUser
import com.unifina.filters.UnifinaCoreAPIFilters
import com.unifina.service.CommunityJoinRequestService
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import spock.lang.Specification

@TestFor(CommunityJoinRequestApiController)
@Mock([UnifinaCoreAPIFilters, SecUser, CommunityJoinRequest])
class CommunityJoinRequestApiControllerSpec extends Specification {
	SecUser me
	final String communityAddress = "0x6c90aece04198da2d5ca9b956b8f95af8041de37"
	final String validID = "L-TvrBkyQTS_JK1ABHFEZAaZ3FHq7-TPqMXe9JNz1x6g"

    def setup() {
		me = new SecUser(id: 1, name: "firstname lastname", username: "firstname.lastname@address.com", password: "salasana")
		me.save(validate: true, failOnError: true)
		controller.communityJoinRequestService = Mock(CommunityJoinRequestService)
    }

	void "state parameter is null or State"(String value, Object expected) {
		expect:
		CommunityJoinRequestApiController.isState(value) == expected
		where:
		value      | expected
		null       | null
		""         | null
		" "        | null
		"\t"       | null
		"abcxyz"   | null
		"accepted" | CommunityJoinRequest.State.ACCEPTED
		"rejected" | CommunityJoinRequest.State.REJECTED
		"pending"  | CommunityJoinRequest.State.PENDING
	}

	void "isCommunityAddress"(String value, Object expected) {
		expect:
		CommunityJoinRequestApiController.isCommunityAddress(value) == expected
		where:
		value | expected
		"0x0000000000000000000000000000000000000000" | true
		"0x0000000000000000000000000000AAAA0000FFFF" | true
		null  | false
		""    | false
		"0x1" | false
	}

	void "findAll() test"() {
		setup:
		CommunityJoinRequest r = new CommunityJoinRequest(
			user: me,
			memberAddress: "0x0000000000000000000000000000000000000001",
			communityAddress: communityAddress,
		)
		r.id = validID
		r.save(failOnError: true, validate: true)
		when:
		request.method = "GET"
		params.communityAddress = communityAddress
		params.state = null
		withFilters(action: "findAll") {
			controller.findAll()
		}
		then:
		1 * controller.communityJoinRequestService.findAll(communityAddress, null) >> [r ]
		response.json[0].id == validID
		response.json[0].memberAddress == "0x0000000000000000000000000000000000000001"
		response.json[0].communityAddress == communityAddress
		response.json[0].state == "PENDING"
		response.status == 200
    }

	void "findAll() bad request on invalid community address input"() {
		when:
		request.method = "GET"
		params.communityAddress = "0x123"
		params.state = "APPROVED"
		withFilters(action: "findAll") {
			controller.findAll()
		}
		then:
		0 * controller.communityJoinRequestService._
		def e = thrown(BadRequestException)
		e.statusCode == 400
		e.code == "PARAMETER_MISSING"
	}

	void "createCommunityJoinRequest() test"() {
		setup:
		CommunityJoinRequest r = new CommunityJoinRequest(
			memberAddress: "0xCCCC000000000000000000000000AAAA0000FFFF",
			communityAddress: communityAddress,
			user: me,
			state: CommunityJoinRequest.State.ACCEPTED,
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
		params.communityAddress = communityAddress
		withFilters(action: "createCommunityJoinRequest") {
			controller.createCommunityJoinRequest()
		}
		then:
		1 * controller.communityJoinRequestService.createCommunityJoinRequest(communityAddress, _ as CommunityJoinRequestCommand, me) >> r
		response.json.id == validID
		response.json.memberAddress == "0xCCCC000000000000000000000000AAAA0000FFFF"
		response.json.communityAddress == communityAddress
		response.json.state == "ACCEPTED"
		response.status == 200
	}

	void "createCommunityJoinRequest() bad request when json memberAddress is not an ethereum address"() {
		when:
		request.method = "POST"
		request.json = [
			memberAddress: "0x123",
			secret: "secret",
		]
		params.communityAddress = communityAddress
		withFilters(action: "createCommunityJoinRequest") {
			controller.createCommunityJoinRequest()
		}
		then:
		0 * controller.communityJoinRequestService._
		def e = thrown(BadRequestException)
		e.statusCode == 400
		e.code == "PARAMETER_MISSING"
	}

	void "createCommunityJoinRequest() bad request on invalid community address input"() {
		when:
		request.method = "GET"
		params.communityAddress = "0x123"
		withFilters(action: "createCommunityJoinRequest") {
			controller.createCommunityJoinRequest()
		}
		then:
		0 * controller.communityJoinRequestService._
		def e = thrown(BadRequestException)
		e.statusCode == 400
		e.code == "PARAMETER_MISSING"
	}

	void "findCommunityJoinRequest() test"() {
		CommunityJoinRequest r = new CommunityJoinRequest(
			memberAddress: "0xCCCC000000000000000000000000AAAA0000FFFF",
			communityAddress: communityAddress,
			user: me,
			state: CommunityJoinRequest.State.ACCEPTED,
		)
		r.id = validID
		r.save(failOnError: true, validate: true)
		when:
		request.method = "GET"
		params.communityAddress = communityAddress
		params.joinRequestId = validID
		withFilters(action: "findCommunityJoinRequest") {
			controller.findCommunityJoinRequest()
		}
		then:
		1 * controller.communityJoinRequestService.findCommunityJoinRequest(communityAddress, validID) >> r
		response.json.memberAddress == "0xCCCC000000000000000000000000AAAA0000FFFF"
		response.json.communityAddress == communityAddress
		response.json.id == validID
		response.json.state == "ACCEPTED"
		response.status == 200
	}

	void "findCommunityJoinRequest() bad request on invalid community address"() {
		when:
		request.method = "GET"
		params.communityAddress = "0x123"
		params.joinRequestId = validID
		withFilters(action: "findCommunityJoinRequest") {
			controller.findCommunityJoinRequest()
		}
		then:
		0 * controller.communityJoinRequestService._
		def e = thrown(BadRequestException)
		e.statusCode == 400
		e.code == "PARAMETER_MISSING"
	}

	void "findCommunityJoinRequest() bad request on invalid community join request id"() {
		when:
		request.method = "GET"
		params.communityAddress = communityAddress
		params.joinRequestId = null
		withFilters(action: "findCommunityJoinRequest") {
			controller.findCommunityJoinRequest()
		}
		then:
		0 * controller.communityJoinRequestService._
		def e = thrown(BadRequestException)
		e.statusCode == 400
		e.code == "PARAMETER_MISSING"
	}

	void "findCommunityJoinRequest() not found 404 on bad CommunityJoinRequest id"() {
		when:
		request.method = "GET"
		params.communityAddress = communityAddress
		params.joinRequestId = validID // ID not found in DB
		withFilters(action: "findCommunityJoinRequest") {
			controller.findCommunityJoinRequest()
		}
		then:
		1 * controller.communityJoinRequestService.findCommunityJoinRequest(communityAddress, validID) >> null
		def e = thrown(NotFoundException)
		e.statusCode == 404
		e.code == "NOT_FOUND"
	}

	void "updateCommunityJoinRequest() test"() {
		setup:
		CommunityJoinRequest r = new CommunityJoinRequest(
			memberAddress: "0xCCCC000000000000000000000000AAAA0000FFFF",
			communityAddress: communityAddress,
			user: me,
			state: CommunityJoinRequest.State.PENDING,
		)
		r.id = validID
		r.save(failOnError: true, validate: true)
		when:
		request.method = "PUT"
		request.json = [
			state: "ACCEPTED",
		]
		params.communityAddress = communityAddress
		params.joinRequestId = validID
		withFilters(action: "updateCommunityJoinRequest") {
			controller.updateCommunityJoinRequest()
		}
		then:
		1 * controller.communityJoinRequestService.updateCommunityJoinRequest(communityAddress, validID, _ as UpdateCommunityJoinRequestCommand) >> {
			r.state = CommunityJoinRequest.State.ACCEPTED
			return r
		}
		response.status == 200
		response.json.id == validID
		response.json.state == "ACCEPTED"
		response.json.memberAddress == "0xCCCC000000000000000000000000AAAA0000FFFF"
		response.json.communityAddress == communityAddress
	}

	void "updateCommunityJoinRequest() bad request on invalid community address"() {
		when:
		request.method = "PUT"
		request.json = [
			state: "ACCEPTED",
		]
		params.communityAddress = "0x123"
		params.joinRequestId = validID
		withFilters(action: "updateCommunityJoinRequest") {
			controller.updateCommunityJoinRequest()
		}
		then:
		0 * controller.communityJoinRequestService._
		def e = thrown(BadRequestException)
		e.statusCode == 400
		e.code == "PARAMETER_MISSING"
	}

	void "updateCommunityJoinRequest() bad request when state is not valid"() {
		when:
		request.method = "PUT"
		request.json = [
			state: "ACCEPTXXX",
		]
		params.communityAddress = communityAddress
		params.joinRequestId = validID
		withFilters(action: "updateCommunityJoinRequest") {
			controller.updateCommunityJoinRequest()
		}
		then:
		0 * controller.communityJoinRequestService._
		def e = thrown(BadRequestException)
		e.statusCode == 400
		e.code == "PARAMETER_MISSING"
	}

	void "updateCommunityJoinRequest() bad request on invalid community join request id"() {
		when:
		request.method = "PUT"
		request.json = [
			state: "ACCEPTED",
		]
		params.communityAddress = communityAddress
		params.joinRequestId = null
		withFilters(action: "updateCommunityJoinRequest") {
			controller.updateCommunityJoinRequest()
		}
		then:
		0 * controller.communityJoinRequestService._
		def e = thrown(BadRequestException)
		e.statusCode == 400
		e.code == "PARAMETER_MISSING"
	}

	void "updateCommunityJoinRequest() not found 404 on bad community join request id"() {
		when:
		request.method = "PUT"
		request.json = [
			state: "ACCEPTED",
		]
		params.communityAddress = communityAddress
		params.joinRequestId = validID // ID not found in DB
		withFilters(action: "updateCommunityJoinRequest") {
			controller.updateCommunityJoinRequest()
		}
		then:
		1 * controller.communityJoinRequestService.updateCommunityJoinRequest(communityAddress, validID, _ as UpdateCommunityJoinRequestCommand) >> {
			throw new NotFoundException("mocked: entity not found")
		}
		def e = thrown(NotFoundException)
		e.statusCode == 404
		e.code == "NOT_FOUND"
	}
}
