package com.unifina.controller.security

import java.util.regex.Pattern

class LoginRedirectValidator {
	// regex is the login redirect url validation regular expression.
	// Accepts:
	// - localhost:port
	// - 127.0.0.1:port
	// - streamr.com and its subdomains
	// Trailing slash is required.
	final static String regex = """
^https://([a-zA-Z0-9-]*\\.)?streamr\\.com(/.*)?     # streamr.com and its subdomains. subdomain == [a-zA-Z0-9-]
|http://localhost:\\d\\d\\d\\d/.*                  # or localhost with any port
|http://127\\.0\\.0\\.1:\\d\\d\\d\\d/.*\$          # or 127.0.0.1 with any port
"""
	final static Pattern pattern = Pattern.compile(regex, Pattern.COMMENTS)
	static boolean isValid(final String url) {
		if (url == null) {
			return false
		}
		def matcher = pattern.matcher(url)
		return matcher.matches()
	}
}
