package com.unifina.controller.security

import spock.lang.Specification

class LoginRedirectValidatorSpec extends Specification {
	List allCombinations(String url) {
		[
		    "$url",
		    "$url/",
		    "$url/foobar",
		    "$url/foobar/",
		    "$url?test=lol",
		    "$url/foobar?test=lol",
			"$url#",
			"$url#moi"
		]
	}

	void "accepts only streamr.com and developer addresses"() {
		expect:
		allCombinations(url).each {
			assert result == LoginRedirectValidator.isValid(it)
		}

		where:
		result | url
		// Don't put trailing slashes or any other endings to the url (added in allCombinations)
		true   | "https://streamr.com"
		true   | "https://www.streamr.com"
		true   | "https://www.marketplace.streamr.com"
		true   | "https://marketplace.streamr.com"
		true   | "https://abc123.streamr.com"
		true   | "https://marketplace-staging.streamr.com"
		true   | "http://localhost"
		true   | "http://localhost:80"
		true   | "http://localhost:7070"
		true   | "http://127.0.0.1:80"
		true   | "http://127.0.0.1:3333"
		false  | "http://www.streamr.com"
		false  | "https://www.steamr.com"
		false  | "https://.streamr.com"
		false  | ""
		false  | null
		false  | "https://www.google.fi"
		false  | "http://localhost:8081test"
		false  | "https://www.streamr.com.phissing.com"
		false  | "ftp://www.streamr.com"
	}
}
