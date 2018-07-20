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
		^(https://([a-zA-Z0-9-]+\\.)*streamr.com				# streamr.com and its subdomains
		|http://(localhost|(127\\.0\\.0\\.1))(:[0-9]{2,5})?)	# localhost or 127.0.0.1 any port
		([/\\#?].*)?\$											# plus any route, query param or hash
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

