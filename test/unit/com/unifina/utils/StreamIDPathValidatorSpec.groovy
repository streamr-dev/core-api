package com.unifina.utils

import org.apache.commons.lang3.StringUtils
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

class StreamIDPathValidatorSpec extends Specification {
	// string120 is 120 chars long
	@Shared
	final String string120 = StringUtils.repeat("xxxxx", 24)

	@Unroll
	def "validates ENS path: #testName"() {
		expect:
		result == StreamIDPathValidator.validate.call(path)

		where:
		result | path                  | testName
		false  | "pathEndsWithSlash/"  | "path ends with slash"
		false  | "pathEndsWithDot."    | "path ends with dot"
		false  | "invalid space chars" | "invalid space char in path"
		false  | string120 + "X"       | "path too long"
		true   | null                  | "null path"
		true   | ""                    | "empty path"
		true   | string120             | "max length path"
		true   | "valid/path"          | "valid path with dir"
		true   | "path"                | "simple path"
		true   | ".valid-path/dir"     | "complex path with allowed chars"
	}
}
