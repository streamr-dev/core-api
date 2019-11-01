package com.unifina.controller.api

import com.unifina.api.*
import com.unifina.domain.community.CommunityJoinRequest
import com.unifina.domain.security.SecUser
import com.unifina.filters.UnifinaCoreAPIFilters
import com.unifina.service.CommunityJoinRequestService
import com.unifina.service.EthereumService
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
		controller.ethereumService = Mock(EthereumService)
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
		request.apiUser = me
		request.method = "GET"
		params.communityAddress = communityAddress
		params.state = null
		withFilters(action: "index") {
			controller.index()
		}
		then:
		1 * controller.ethereumService.fetchCommunityAdminsEthereumAddress(communityAddress) >> "adminAddress"
		1 * controller.ethereumService.hasEthereumAddress(me, "adminAddress") >> true
		1 * controller.communityJoinRequestService.findAll(communityAddress, null) >> [r]
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
		withFilters(action: "index") {
			controller.index()
		}
		then:
		0 * controller.communityJoinRequestService._
		def e = thrown(BadRequestException)
		e.statusCode == 400
		e.code == "PARAMETER_MISSING"
	}

	void "findAll() checks admin access control"() {
		when:
		request.apiUser = me
		request.method = "GET"
		params.communityAddress = communityAddress
		params.state = "APPROVED"
		withFilters(action: "index") {
			controller.index()
		}
		then:
		1 * controller.ethereumService.fetchCommunityAdminsEthereumAddress(communityAddress) >> "adminAddress"
		1 * controller.ethereumService.hasEthereumAddress(me, "adminAddress") >> false
		def e = thrown(ApiException)
		e.statusCode == 403
		e.code == "FORBIDDEN"
	}

	void "save() test"() {
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
		withFilters(action: "save") {
			controller.save()
		}
		then:
		1 * controller.communityJoinRequestService.create(communityAddress, _ as CommunityJoinRequestCommand, me) >> r
		response.json.id == validID
		response.json.memberAddress == "0xCCCC000000000000000000000000AAAA0000FFFF"
		response.json.communityAddress == communityAddress
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
		params.communityAddress = communityAddress
		withFilters(action: "save") {
			controller.save()
		}
		then:
		0 * controller.communityJoinRequestService._
		def e = thrown(BadRequestException)
		e.statusCode == 400
		e.code == "PARAMETER_MISSING"
	}

	void "save() bad request on invalid community address input"() {
		when:
		request.method = "GET"
		params.communityAddress = "0x123"
		withFilters(action: "save") {
			controller.save()
		}
		then:
		0 * controller.communityJoinRequestService._
		def e = thrown(BadRequestException)
		e.statusCode == 400
		e.code == "PARAMETER_MISSING"
	}

	void "show() test"() {
		CommunityJoinRequest r = new CommunityJoinRequest(
			memberAddress: "0xCCCC000000000000000000000000AAAA0000FFFF",
			communityAddress: communityAddress,
			user: me,
			state: CommunityJoinRequest.State.ACCEPTED,
		)
		r.id = validID
		r.save(failOnError: true, validate: true)
		when:
		request.apiUser = me
		request.method = "GET"
		params.communityAddress = communityAddress
		params.id = validID
		withFilters(action: "show") {
			controller.show()
		}
		then:
		1 * controller.ethereumService.fetchCommunityAdminsEthereumAddress(communityAddress) >> "adminAddress"
		1 * controller.ethereumService.hasEthereumAddress(me, "adminAddress") >> true
		1 * controller.communityJoinRequestService.find(communityAddress, validID) >> r
		response.json.memberAddress == "0xCCCC000000000000000000000000AAAA0000FFFF"
		response.json.communityAddress == communityAddress
		response.json.id == validID
		response.json.state == "ACCEPTED"
		response.status == 200
	}

	void "show() bad request on invalid community address"() {
		when:
		request.method = "GET"
		params.communityAddress = "0x123"
		params.id = validID
		withFilters(action: "show") {
			controller.show()
		}
		then:
		0 * controller.communityJoinRequestService._
		def e = thrown(BadRequestException)
		e.statusCode == 400
		e.code == "PARAMETER_MISSING"
	}

	void "show() bad request on invalid community join request id"() {
		when:
		request.method = "GET"
		params.communityAddress = communityAddress
		params.id = null
		withFilters(action: "show") {
			controller.show()
		}
		then:
		0 * controller.communityJoinRequestService._
		def e = thrown(BadRequestException)
		e.statusCode == 400
		e.code == "PARAMETER_MISSING"
	}

	void "show() not found 404 on bad CommunityJoinRequest id"() {
		when:
		request.apiUser = me
		request.method = "GET"
		params.communityAddress = communityAddress
		params.id = validID // ID not found in DB
		withFilters(action: "show") {
			controller.show()
		}
		then:
		1 * controller.ethereumService.fetchCommunityAdminsEthereumAddress(communityAddress) >> "adminAddress"
		1 * controller.ethereumService.hasEthereumAddress(me, "adminAddress") >> true
		1 * controller.communityJoinRequestService.find(communityAddress, validID) >> null
		def e = thrown(NotFoundException)
		e.statusCode == 404
		e.code == "NOT_FOUND"
	}

	void "show() checks admin access control"() {
		when:
		request.apiUser = me
		request.method = "GET"
		params.communityAddress = communityAddress
		params.id = validID
		withFilters(action: "show") {
			controller.show()
		}
		then:
		1 * controller.ethereumService.fetchCommunityAdminsEthereumAddress(communityAddress) >> "adminAddress"
		1 * controller.ethereumService.hasEthereumAddress(me, "adminAddress") >> false
		def e = thrown(ApiException)
		e.statusCode == 403
		e.code == "FORBIDDEN"
	}

	void "update() test"() {
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
		request.apiUser = me
		request.method = "PUT"
		request.json = [
			state: "ACCEPTED",
		]
		params.communityAddress = communityAddress
		params.id = validID
		withFilters(action: "update") {
			controller.update()
		}
		then:
		1 * controller.ethereumService.fetchCommunityAdminsEthereumAddress(communityAddress) >> "adminAddress"
		1 * controller.ethereumService.hasEthereumAddress(me, "adminAddress") >> true
		1 * controller.communityJoinRequestService.update(communityAddress, validID, _ as UpdateCommunityJoinRequestCommand) >> {
			r.state = CommunityJoinRequest.State.ACCEPTED
			return r
		}
		response.status == 200
		response.json.id == validID
		response.json.state == "ACCEPTED"
		response.json.memberAddress == "0xCCCC000000000000000000000000AAAA0000FFFF"
		response.json.communityAddress == communityAddress
	}

	void "update() bad request on invalid community address"() {
		when:
		request.method = "PUT"
		request.json = [
			state: "ACCEPTED",
		]
		params.communityAddress = "0x123"
		params.id = validID
		withFilters(action: "update") {
			controller.update()
		}
		then:
		0 * controller.communityJoinRequestService._
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
		params.communityAddress = communityAddress
		params.id = validID
		withFilters(action: "update") {
			controller.update()
		}
		then:
		0 * controller.communityJoinRequestService._
		def e = thrown(BadRequestException)
		e.statusCode == 400
		e.code == "PARAMETER_MISSING"
	}

	void "update() bad request on invalid community join request id"() {
		when:
		request.method = "PUT"
		request.json = [
			state: "ACCEPTED",
		]
		params.communityAddress = communityAddress
		params.id = null
		withFilters(action: "update") {
			controller.update()
		}
		then:
		0 * controller.communityJoinRequestService._
		def e = thrown(BadRequestException)
		e.statusCode == 400
		e.code == "PARAMETER_MISSING"
	}

	void "update() not found 404 on bad community join request id"() {
		when:
		request.apiUser = me
		request.method = "PUT"
		request.json = [
			state: "ACCEPTED",
		]
		params.communityAddress = communityAddress
		params.id = validID // ID not found in DB
		withFilters(action: "update") {
			controller.update()
		}
		then:
		1 * controller.ethereumService.fetchCommunityAdminsEthereumAddress(communityAddress) >> "adminAddress"
		1 * controller.ethereumService.hasEthereumAddress(me, "adminAddress") >> true
		1 * controller.communityJoinRequestService.update(communityAddress, validID, _ as UpdateCommunityJoinRequestCommand) >> {
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
		params.communityAddress = communityAddress
		params.id = validID
		withFilters(action: "update") {
			controller.update()
		}
		then:
		1 * controller.ethereumService.fetchCommunityAdminsEthereumAddress(communityAddress) >> "adminAddress"
		1 * controller.ethereumService.hasEthereumAddress(me, "adminAddress") >> false
		def e = thrown(ApiException)
		e.statusCode == 403
		e.code == "FORBIDDEN"
	}

	void "delete() test"() {
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
		request.apiUser = me
		request.method = "DELETE"
		params.communityAddress = communityAddress
		params.id = validID
		withFilters(action: "delete") {
			controller.delete()
		}
		then:
		1 * controller.ethereumService.fetchCommunityAdminsEthereumAddress(communityAddress) >> "adminAddress"
		1 * controller.ethereumService.hasEthereumAddress(me, "adminAddress") >> true
		1 * controller.communityJoinRequestService.delete(communityAddress, validID)
		response.status == 204
	}

	void "delete() bad request on invalid community address"() {
		when:
		request.method = "DELETE"
		params.communityAddress = "0x123"
		params.id = validID
		withFilters(action: "delete") {
			controller.delete()
		}
		then:
		0 * controller.communityJoinRequestService._
		def e = thrown(BadRequestException)
		e.statusCode == 400
		e.code == "PARAMETER_MISSING"
	}

	void "delete() bad request on invalid community join request id"() {
		when:
		request.method = "DELETE"
		params.communityAddress = communityAddress
		params.id = "0x123"
		withFilters(action: "delete") {
			controller.delete()
		}
		then:
		0 * controller.communityJoinRequestService._
		def e = thrown(BadRequestException)
		e.statusCode == 400
		e.code == "PARAMETER_MISSING"
	}

	void "delete() not found 404 on bad community join request id"() {
		when:
		request.apiUser = me
		request.method = "DELETE"
		params.communityAddress = communityAddress
		params.id = validID // ID not found in DB
		withFilters(action: "delete") {
			controller.delete()
		}
		then:
		1 * controller.ethereumService.fetchCommunityAdminsEthereumAddress(communityAddress) >> "adminAddress"
		1 * controller.ethereumService.hasEthereumAddress(me, "adminAddress") >> true
		1 * controller.communityJoinRequestService.delete(communityAddress, validID) >> {
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
		params.communityAddress = communityAddress
		params.id = validID
		withFilters(action: "delete") {
			controller.delete()
		}
		then:
		1 * controller.ethereumService.fetchCommunityAdminsEthereumAddress(communityAddress) >> "adminAddress"
		1 * controller.ethereumService.hasEthereumAddress(me, "adminAddress") >> false
		def e = thrown(ApiException)
		e.statusCode == 403
		e.code == "FORBIDDEN"
	}
}
