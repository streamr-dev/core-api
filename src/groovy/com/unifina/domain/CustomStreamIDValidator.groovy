package com.unifina.domain

import java.util.regex.Matcher
import java.util.regex.Pattern
import grails.compiler.GrailsCompileStatic

@GrailsCompileStatic
class CustomStreamIDValidator {

	interface DomainValidator {
		boolean isOwnedBy(String domain, User owner)
	}

	// path rules:
	// - must start by slash
	// - can contain chars a-z, A-Z, 0-9 and -_.
	// - must not end with non-word character (in this case - or .)
	// - can have segments separated by slashes (two consecutive slashes is not allowed)
	public static final Pattern REGEX = Pattern.compile("^((?:[\\w-]+\\.?)*\\w)/(?:[\\w\\.-]+/?)*\\w\$")

	DomainValidator domainValidator

	CustomStreamIDValidator(DomainValidator domainValidator) {
		this.domainValidator = domainValidator;
	}

	boolean validate(String id, User creator) {
		if (id == null) {
			return true
		} else {
			Matcher matcher = REGEX.matcher(id)
			if (matcher.matches()) {
				String domain = matcher.group(1)
				return domainValidator.isOwnedBy(domain, creator)
			} else {
				return false;
			}
		}
	}
}
