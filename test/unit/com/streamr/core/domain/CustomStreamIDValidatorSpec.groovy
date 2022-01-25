package com.streamr.core.domain
import com.streamr.core.domain.CustomStreamIDValidator
import org.apache.commons.lang3.StringUtils
import spock.lang.Specification
import spock.lang.Unroll

class CustomStreamIDValidatorSpec extends Specification {

	private static final String MOCK_ENS_DOMAIN = "sub.my-domain.eth"
	private static final String MOCK_INTEGRATION_KEY_ADDRESS = "0xAbcdeabCDE123456789012345678901234567890"

	@Unroll
	void "validate #value"(String value, Boolean expected) {
		def User mockUser = new User()
		def domainValidator = { domain, creator -> ((creator == mockUser) && (domain.equals(MOCK_ENS_DOMAIN) || domain.equalsIgnoreCase(MOCK_INTEGRATION_KEY_ADDRESS))) }
		def validator = new CustomStreamIDValidator(domainValidator)
		expect:
		validator.validate(value, mockUser) == expected
		where:
		value                                                                        | expected
		"sub.my-domain.eth/a"                                                        | true
		"sub.my-domain.eth/abc/def"                                                  | true
		"sub.my-domain.eth/abc/def/file.txt"                                         | true
		"sub.my-domain.eth/foo.bar/lorem.ipsum"                                      | true
		"sub.my-domain.eth/foo-bar/lorem.ipsum"                                      | true
		"sub.my-domain.eth/abc/def"                                                  | true
		"0xAbcdeabCDE123456789012345678901234567890/abc/def"                         | true
		"0xaBCDEABcde123456789012345678901234567890/abc/def"                         | true
		"sub.my-domain.eth/foo-bar/lorem~ipsum"                                      | false
		"sub.my-domain.eth/abc/def/"                                                 | false
		"sub.my-domain.eth/abc/def/file."                                            | false
		"sub.my-domain.eth/foo//bar"                                                 | false
		"foobar.eth/abc/def"                                                         | false
		"sub.my-domain.eth"                                                          | false
		"0xAbcdeabCDE123456789012345678901234567890"                                 | false
		"0xAbcdeabCDE123456789012345678901234567890/" + StringUtils.repeat("x", 300) | false
		"0x1111111111111111111111111111111111111111/abc/def"                         | false
		"foo"                                                                        | false
		""                                                                           | false
		" "                                                                          | false
		null                                                                         | false
	}
}
