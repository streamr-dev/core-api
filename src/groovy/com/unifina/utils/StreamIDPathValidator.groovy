package com.unifina.utils

import grails.compiler.GrailsCompileStatic

import java.util.regex.Matcher
import java.util.regex.Pattern

@GrailsCompileStatic
class StreamIDPathValidator {
	private static final int MAX_LENGTH = 120
	private static final String REGEX = """
		^
		[A-Za-z0-9-\\./]{0,""" + MAX_LENGTH + """} # path
		[^/\\.] # path name last char can't be hyphen or dot
		\$
	"""

	private static final Pattern PATTERN = Pattern.compile(REGEX, Pattern.COMMENTS)

	static final Closure validate = { String path ->
		if (path == null || path == "") {
			return true
		}
		if (path.length() > MAX_LENGTH) {
			return false
		}
		Matcher matcher = PATTERN.matcher(path)
		boolean result = matcher.matches()
		return result
	}
}
