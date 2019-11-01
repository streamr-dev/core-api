package com.unifina.utils

import spock.lang.Specification

class HexIdGeneratorSpec extends Specification {

	HexIdGenerator generator = new HexIdGenerator();

	def "Generate"() {
		String id = generator.generate(null, null)

		expect:
		id.matches(/[0-9a-f]{64}/)
	}
}
