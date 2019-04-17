package com.unifina.controller.api

import com.unifina.api.BadRequestException
import com.unifina.api.CommunityJoinRequestCommand
import com.unifina.api.NotFoundException
import com.unifina.api.UpdateCommunityJoinRequestCommand
import com.unifina.domain.community.CommunityJoinRequest
import com.unifina.domain.security.SecUser
import com.unifina.filters.UnifinaCoreAPIFilters
import com.unifina.service.CommunityProductService
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import spock.lang.Specification

@TestFor(CommunityProductApiController)
@Mock([UnifinaCoreAPIFilters, SecUser, CommunityJoinRequest])
class CommunityProductApiControllerSpec extends Specification {
	SecUser me
	final String communityAddress = "0x6c90aece04198da2d5ca9b956b8f95af8041de37"
	final String communityJoinRequestId = "commmunity-join-request-id"

    def setup() {
		me = new SecUser(id: 1, name: "firstname lastname", username: "firstname.lastname@address.com", password: "salasana")
		me.save(validate: true, failOnError: true)
		controller.communityProductService = Mock(CommunityProductService)
    }

	void "state parameter is null or State"(String value, Object expected) {
		expect:
		CommunityProductApiController.isState(value) == expected
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
		CommunityProductApiController.isCommunityAddress(value) == expected
		where:
		value | expected
		"0x0000000000000000000000000000000000000000" | true
		"0x0000000000000000000000000000AAAA0000FFFF" | true
		null  | false
		""    | false
		"0x1" | false
	}

	void "isJoinRequestId"(String value, Object expected) {
		expect:
		CommunityProductApiController.isJoinRequestId(value) == expected
		where:
		value | expected
		null  | false
		""    | true
		"x"   | true
	}

	void "findCommunityJoinRequests() test"() {
		setup:
		CommunityJoinRequest r = new CommunityJoinRequest(
			user: me,
			memberAddress: "0x0000000000000000000000000000000000000001",
			communityAddress: communityAddress,
		)
		r.id = communityJoinRequestId
		r.save(failOnError: true, validate: true)
		when:
		request.method = "GET"
		params.communityAddress = communityAddress
		params.state = null
		withFilters(action: "findCommunityJoinRequests") {
			controller.findCommunityJoinRequests()
		}
		then:
		1 * controller.communityProductService.findCommunityJoinRequests(communityAddress, null) >> [r ]
		response.json[0].id == communityJoinRequestId
		response.json[0].memberAddress == "0x0000000000000000000000000000000000000001"
		response.json[0].communityAddress == communityAddress
		response.json[0].state == "PENDING"
		response.status == 200
    }

	void "findCommunityJoinRequests() bad request on invalid community address input"() {
		when:
		request.method = "GET"
		params.communityAddress = "0x123"
		params.state = "APPROVED"
		withFilters(action: "findCommunityJoinRequests") {
			controller.findCommunityJoinRequests()
		}
		then:
		0 * controller.communityProductService._
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
		r.id = communityJoinRequestId
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
		1 * controller.communityProductService.createCommunityJoinRequest(communityAddress, _ as CommunityJoinRequestCommand, me) >> r
		response.json.id == communityJoinRequestId
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
		0 * controller.communityProductService._
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
		0 * controller.communityProductService._
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
		r.id = communityJoinRequestId
		r.save(failOnError: true, validate: true)
		when:
		request.method = "GET"
		params.communityAddress = communityAddress
		params.joinRequestId = communityJoinRequestId
		withFilters(action: "findCommunityJoinRequest") {
			controller.findCommunityJoinRequest()
		}
		then:
		1 * controller.communityProductService.findCommunityJoinRequest(communityAddress, communityJoinRequestId) >> r
		response.json.memberAddress == "0xCCCC000000000000000000000000AAAA0000FFFF"
		response.json.communityAddress == communityAddress
		response.json.id == communityJoinRequestId
		response.json.state == "ACCEPTED"
		response.status == 200
	}

	void "findCommunityJoinRequest() bad request on invalid community address"() {
		when:
		request.method = "GET"
		params.communityAddress = "0x123"
		params.joinRequestId = communityJoinRequestId
		withFilters(action: "findCommunityJoinRequest") {
			controller.findCommunityJoinRequest()
		}
		then:
		0 * controller.communityProductService._
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
		0 * controller.communityProductService._
		def e = thrown(BadRequestException)
		e.statusCode == 400
		e.code == "PARAMETER_MISSING"
	}

	void "findCommunityJoinRequest() not found 404 on bad CommunityJoinRequest id"() {
		when:
		request.method = "GET"
		params.communityAddress = communityAddress
		params.joinRequestId = "not-found"
		withFilters(action: "findCommunityJoinRequest") {
			controller.findCommunityJoinRequest()
		}
		then:
		1 * controller.communityProductService.findCommunityJoinRequest(communityAddress,"not-found") >> null
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
		r.id = communityJoinRequestId
		r.save(failOnError: true, validate: true)
		when:
		request.method = "PUT"
		request.json = [
			state: "ACCEPTED",
		]
		params.communityAddress = communityAddress
		params.joinRequestId = communityJoinRequestId
		withFilters(action: "updateCommunityJoinRequest") {
			controller.updateCommunityJoinRequest()
		}
		then:
		1 * controller.communityProductService.updateCommunityJoinRequest(communityAddress, communityJoinRequestId, _ as UpdateCommunityJoinRequestCommand) >> r
		response.status == 204
	}

	void "updateCommunityJoinRequest() bad request on invalid community address"() {
		when:
		request.method = "PUT"
		request.json = [
			state: "ACCEPTED",
		]
		params.communityAddress = "0x123"
		params.joinRequestId = communityJoinRequestId
		withFilters(action: "updateCommunityJoinRequest") {
			controller.updateCommunityJoinRequest()
		}
		then:
		0 * controller.communityProductService._
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
		params.joinRequestId = communityJoinRequestId
		withFilters(action: "updateCommunityJoinRequest") {
			controller.updateCommunityJoinRequest()
		}
		then:
		0 * controller.communityProductService._
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
		0 * controller.communityProductService._
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
		params.joinRequestId = "not-found"
		withFilters(action: "updateCommunityJoinRequest") {
			controller.updateCommunityJoinRequest()
		}
		then:
		1 * controller.communityProductService.updateCommunityJoinRequest(communityAddress, "not-found", _ as UpdateCommunityJoinRequestCommand) >> null
		def e = thrown(NotFoundException)
		e.statusCode == 404
		e.code == "NOT_FOUND"
	}
}
