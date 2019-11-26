package com.unifina.controller.api

import com.unifina.api.BadRequestException
import com.unifina.filters.UnifinaCoreAPIFilters
import com.unifina.cps.CommunityOperatorServiceImpl
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import spock.lang.Specification

@TestFor(CommunityOperatorApiController)
@Mock([UnifinaCoreAPIFilters])
class CommunityOperatorApiControllerSpec extends Specification {
	final String communityAddress = "0x6c90aece04198da2d5ca9b956b8f95af8041de37"
	final String memberAddress = "0x7d01bfdf15198da2d5ca9b956c8f95af0041de38"

    def setup() {
		controller.communityOperatorService = Mock(CommunityOperatorServiceImpl)
    }

    void "test stats"() {
		when:
		request.method = "GET"
		params.communityAddress = communityAddress
		withFilters(action: "stats") {
			controller.stats()
		}
		then:
		1 * controller.communityOperatorService.stats(communityAddress) >> new CommunityOperatorServiceImpl.ProxyResponse(statusCode: 200, body: """{"stats":[]}""")
		response.json == [stats: []]
		response.status == 200
    }

	void "test stats service returns 500 internal server error"() {
		when:
		request.method = "GET"
		params.communityAddress = communityAddress
		withFilters(action: "stats") {
			controller.stats()
		}
		then:
		1 * controller.communityOperatorService.stats(communityAddress) >> new CommunityOperatorServiceImpl.ProxyResponse(statusCode: 500)
		response.status == 500
		response.text == ""
	}

	void "stats bad request on invalid community address input"() {
		when:
		request.method = "GET"
		params.communityAddress = "0x123"
		withFilters(action: "stats") {
			controller.stats()
		}
		then:
		0 * controller.communityOperatorService._
		def e = thrown(BadRequestException)
		e.statusCode == 400
		e.code == "PARAMETER_MISSING"
	}

	void "test members"() {
		when:
		request.method = "GET"
		params.communityAddress = communityAddress
		withFilters(action: "members") {
			controller.members()
		}
		then:
		1 * controller.communityOperatorService.members(communityAddress) >> new CommunityOperatorServiceImpl.ProxyResponse(statusCode: 200, body: """{"members":[]}""")
		response.json == [members: []]
		response.status == 200
	}

	void "members bad request on invalid community address input"() {
		when:
		request.method = "GET"
		params.communityAddress = "0x123"
		withFilters(action: "members") {
			controller.members()
		}
		then:
		0 * controller.communityOperatorService._
		def e = thrown(BadRequestException)
		e.statusCode == 400
		e.code == "PARAMETER_MISSING"
	}

	void "test memberStats"() {
		when:
		request.method = "GET"
		params.communityAddress = communityAddress
		params.memberAddress = memberAddress
		withFilters(action: "memberStats()") {
			controller.memberStats()
		}
		then:
		1 * controller.communityOperatorService.memberStats(communityAddress, memberAddress) >> new CommunityOperatorServiceImpl.ProxyResponse(statusCode: 200, body: """{"memberStats":[]}""")
		response.json == [memberStats: []]
		response.status == 200
	}

	void "memberStats bad request on invalid community address input"() {
		when:
		request.method = "GET"
		params.communityAddress = "0x123"
		withFilters(action: "memberStats()") {
			controller.memberStats()
		}
		then:
		0 * controller.communityOperatorService._
		def e = thrown(BadRequestException)
		e.statusCode == 400
		e.code == "PARAMETER_MISSING"
	}

	void "memberStats bad request on invalid member address input"() {
		when:
		request.method = "GET"
		params.communityAddress = communityAddress
		params.memberAddress = "0x123"
		withFilters(action: "memberStats()") {
			controller.memberStats()
		}
		then:
		0 * controller.communityOperatorService._
		def e = thrown(BadRequestException)
		e.statusCode == 400
		e.code == "PARAMETER_MISSING"
	}
}
