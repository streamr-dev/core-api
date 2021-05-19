package com.unifina.domain

import java.util.regex.Matcher
import java.util.regex.Pattern
import grails.compiler.GrailsCompileStatic

@GrailsCompileStatic
class CustomStreamIDValidator {

	interface DomainValidator {
		boolean isOwnedBy(String domain, User owner)
	}

	// ENS domain rules:
	// - must contain two or more segments separated by dot
	// - segments can contain chars a-z, A-Z, 0-9 and -
	// https://docs.ens.domains/contract-api-reference/name-processing
	private static final String ENS_DOMAIN_REGEX = "(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z0-9-]+"

	// path rules:
	// - must start by slash
	// - can contain chars a-z, A-Z, 0-9 and -_.
	// - must not end with non-word character (in this case - or .)
	// - can have segments separated by slashes (two consecutive slashes is not allowed)
	private static final String PATH_REGEX = "(?:(?:/[\\w\\.-]+)+\\w)|(?:/\\w)"

	private static final String ETHEREUM_ADDRESS_REGEX = "0x[a-fA-F0-9]{40}"
	private static final Pattern STREAM_ID_REGEX = Pattern.compile("^((?:" + ETHEREUM_ADDRESS_REGEX + ")|(?:" + ENS_DOMAIN_REGEX +"))(?:" + PATH_REGEX + ")\$")

	private static final int MAX_LENGTH = 255

	DomainValidator domainValidator

	CustomStreamIDValidator(DomainValidator domainValidator) {
		this.domainValidator = domainValidator;
	}

	boolean validate(String id, User creator) {
		if (id == null) {
			return true
		} else {
			if (id.length() <= MAX_LENGTH) {
				Matcher matcher = STREAM_ID_REGEX.matcher(id)
				if (matcher.matches()) {
					String domain = matcher.group(1)
					return domainValidator.isOwnedBy(domain, creator)
				}
			}
			return false;
		}
	}
}
