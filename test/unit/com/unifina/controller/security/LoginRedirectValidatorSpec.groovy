package com.unifina.controller.security

import spock.lang.Specification

class LoginRedirectValidatorSpec extends Specification {
	void "accepts only streamr.com and developer addresses"() {
		expect:
		result == LoginRedirectValidator.isValid(url)

		where:
		result | url
		true   | "https://www.streamr.com/"
		true   | "https://www.streamr.com/page.html"
		true   | "https://abc123.streamr.com/"
		true   | "https://www.streamr.com/?foo=bar&bar=foo"
		true   | "http://localhost:8080/"
		true   | "http://localhost:8080/?foo=bar&bar=foo"
		true   | "http://localhost:7070/?foo=bar&bar=foo"
		true   | "http://127.0.0.1:3333/"
		true   | "http://127.0.0.1:3333/index.html?foobar=lol"
		true   | "http://127.0.0.1:3131/index.html?foobar=lol"
		true   | "https://www.streamr.com"
		true   | "https://marketplace.streamr.com"
		true   | "https://marketplace-staging.streamr.com"
		true   | "https://marketplace.streamr.com/sub"
		true   | "https://streamr.com"
		true   | "https://streamr.com/"
		true   | "https://streamr.com/marketplace"
		false  | "http://www.streamr.com/"
		false  | ""
		false  | null
		false  | "https://www.google.fi/"
		false  | "https://www.streamr.com.phissing.com"
		false  | "https://www.streamr.com.phissing.com/"
		false  | "https://www.streamr.com.phissing.com/sub"
	}
}
