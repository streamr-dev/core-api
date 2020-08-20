package com.unifina.utils

import grails.compiler.GrailsCompileStatic

import java.util.regex.Matcher
import java.util.regex.Pattern

@GrailsCompileStatic
class ENSNameValidator {

	/*
	 * https://eips.ethereum.org/EIPS/eip-137
	 *
	 * Labels and domains may be of any length, but for compatibility with legacy DNS, it is recommended that labels
	 * be restricted to no more than 64 characters each, and complete ENS names to no more than 255 characters.
	 * For the same reason, it is recommended that labels do not start or end with hyphens, or start with digits.
	 */

	private static final int LABEL_MAX_LENGTH = 64
	private static final String REGEX = """
		^
		[^-] # domain name first char can't be hyphen
		[A-Za-z0-9-]{1,""" + LABEL_MAX_LENGTH + """} # domain name (label)
		[^-] # domain name last char can't be hyphen
		\\. # dot
		[A-Za-z]{2,""" + LABEL_MAX_LENGTH + """} # tld (label)
		\$ 
	"""

	private static final Pattern PATTERN = Pattern.compile(REGEX, Pattern.COMMENTS)
	private static final int MAX_LENGTH = 129
	private static final int MIN_LENGTH = 4

	static final Closure validate = { String name ->
		if (name == null) {
			return false
		}
		if (name.length() <= MIN_LENGTH || name.length() > MAX_LENGTH) {
			return false
		}
		Matcher matcher = PATTERN.matcher(name)
		boolean result = matcher.matches()
		return result
	}
}
