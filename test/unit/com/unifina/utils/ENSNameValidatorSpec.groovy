package com.unifina.utils

import org.apache.commons.lang3.StringUtils
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

class ENSNameValidatorSpec extends Specification {
	// string64 is 64 chars long
	@Shared
	final String string64 = StringUtils.repeat("xxxxxxxx", 8)

	@Unroll
	void "validates ENS domain name: #testName"() {
		expect:
		result == ENSNameValidator.validate.call(name)

		where:
		result | name | testName
		false  | "-no-prefix-hyphen.eth" | "no prefix hyphen"
		false  | "no-postfix-hyphen-.eth" | "no postfix hyphen"
		false  | string64 + "A.A" + string64 | "domain and tld labels too long"
		false  | string64 + "A." + string64 | "domain label too long"
		false  | string64 + ".A" + string64 | "tld label too long"
		false  | "no-dot-eth" | "no dot in domain name"
		false  | null | "null path"
		true   | string64 + "." + string64 | "max length"
		true   | "streamr.eth" | "valid case 1"
		true   | "streamr.test" | "valid case 2"
		true   | "data-coin.eth" | "valid domain with hyphen"
	}
}
