package com.unifina.domain

import grails.test.mixin.TestFor
import spock.lang.Specification

@TestFor(DataUnionJoinRequest)
class DataUnionJoinRequestSpec extends Specification {
	DataUnionJoinRequest req
    User me

    def setup() {
		me = new User(
			id: "1",
			username: "email@address.com",
			password: "123",
			name: "Streamr User",
		)
		req = new DataUnionJoinRequest(
			id: "1",
			user: me,
			memberAddress: "0xfffFFffFfffFffffFFFFffffFFFFfffFFFFfffFf",
			contractAddress: "0x0123456789abcdefABCDEF000000000000000000",
			state: DataUnionJoinRequest.State.PENDING,
			dateCreated: new Date(),
			lastUpdated: new Date(),
		)
    }

	void "state parameter is null or State"(String value, Object expected) {
		expect:
		DataUnionJoinRequest.State.isState(value) == expected
		where:
		value      | expected
		null       | null
		""         | null
		" "        | null
		"\t"       | null
		"abcxyz"   | null
		"accepted" | DataUnionJoinRequest.State.ACCEPTED
		"rejected" | DataUnionJoinRequest.State.REJECTED
		"pending"  | DataUnionJoinRequest.State.PENDING
	}

	void "valid DataUnionJoinRequest validates ok"() {
		setup:
		req.dateCreated = new Date()
		req.lastUpdated = new Date()
		when:
		def result = req.validate()
		then:
		result
		req.errors.errorCount == 0
	}

	void "user cannot be null"() {
		setup:
		req.dateCreated = new Date()
		req.lastUpdated = new Date()
		when:
		req.user = null
		def result = req.validate()
		then:
		!result
		req.errors.errorCount == 1
		req.errors.fieldErrors.get(0).field == "user"
	}

    void "memberAddress must be an Ethereum address"() {
		setup:
		req.dateCreated = new Date()
		req.lastUpdated = new Date()
		when:
		req.memberAddress = "x"
		def result = req.validate()
		then:
		!result
		req.errors.errorCount == 1
		req.errors.fieldErrors.get(0).field == "memberAddress"
	}

	void "memberAddress cannot be null"() {
		setup:
		req.dateCreated = new Date()
		req.lastUpdated = new Date()
		when:
		req.memberAddress = null
		def result = req.validate()
		then:
		!result
		req.errors.errorCount == 1
		req.errors.fieldErrors.get(0).field == "memberAddress"
	}

	void "contractAddress must be an Ethereum address"() {
		setup:
		req.dateCreated = new Date()
		req.lastUpdated = new Date()
		when:
		req.contractAddress = "x"
		def result = req.validate()
		then:
		!result
		req.errors.errorCount == 1
		req.errors.fieldErrors.get(0).field == "contractAddress"
	}

	void "contractAddress cannot be null"() {
		setup:
		req.dateCreated = new Date()
		req.lastUpdated = new Date()
		when:
		req.contractAddress = null
		def result = req.validate()
		then:
		!result
		req.errors.errorCount == 1
		req.errors.fieldErrors.get(0).field == "contractAddress"
	}

	void "state cannot be null"() {
		setup:
		req.dateCreated = new Date()
		req.lastUpdated = new Date()
		when:
		req.state = null
		def result = req.validate()
		then:
		!result
	}
}
