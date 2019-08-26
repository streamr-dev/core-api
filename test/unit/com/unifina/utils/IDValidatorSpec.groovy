package com.unifina.utils

import spock.lang.Specification

class IDValidatorSpec extends Specification {
	void "id validator"(String value, Boolean expected) {
		expect:
		IDValidator.validate(value) == expected
		where:
		value | expected
		null | false
		"" | false
		"x" | false
		"\n" | false
		"L-TvrBkyQTS_JK1ABHFEZAaZ3FHq7-TPqMXe9JNz1x6gg" | false
		"L-TvrBkyQTS_JK1ABHFEZAaZ3FHq7-TPqMXe9JNz1x6g" | true
		"-P88Rnn-SryR9hFTThLn4g_RVMUitQQOedsOiFpsJgMw" | true
		"24kQ842bRhmFEJVuiP6hhQfqQTOlI8ShCJXkpNoW51VQ" | true
		"pLHxl9WETeab-T-znW-Sbw-tEtvL57RC6th61_bMiAMQ" | true
		"491k14P3RwCQHl2u6QyTNwvD7NucjwQJqi_V37dDS_sw" | true
	}
}
