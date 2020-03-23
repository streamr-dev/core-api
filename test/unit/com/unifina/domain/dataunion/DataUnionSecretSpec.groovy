package com.unifina.domain.dataunion

import grails.test.mixin.TestFor
import spock.lang.Specification

@TestFor(DataUnionSecret)
class DataUnionSecretSpec extends Specification {
	DataUnionSecret sec

    def setup() {
		sec = new DataUnionSecret(
			id: "1",
			name: "Name of the secret",
			secret: "xxxxxxx",
			contractAddress: "0x0123456789abcdefABCDEF000000000000000000",
		)
    }

	void "valid DataUnionSecret validates ok"() {
		when:
		def result = sec.validate()
		then:
		result
		sec.errors.errorCount == 0
	}

	void "name cannot be null"() {
		when:
		sec.name = null
		def result = sec.validate()
		then:
		!result
		sec.errors.errorCount == 1
		sec.errors.fieldErrors.get(0).field == "name"
	}

	void "secret cannot be null"() {
		when:
		sec.secret = null
		def result = sec.validate()
		then:
		!result
		sec.errors.errorCount == 1
		sec.errors.fieldErrors.get(0).field == "secret"
	}

	void "contractAddress must be an Ethereum address"() {
		when:
		sec.contractAddress = "x"
		def result = sec.validate()
		then:
		!result
		sec.errors.errorCount == 1
		sec.errors.fieldErrors.get(0).field == "contractAddress"
	}

	void "contractAddress cannot be null"() {
		when:
		sec.contractAddress = null
		def result = sec.validate()
		then:
		!result
		sec.errors.errorCount == 1
		sec.errors.fieldErrors.get(0).field == "contractAddress"
	}
}
