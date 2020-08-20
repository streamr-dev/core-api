package com.unifina.domain

import com.unifina.utils.ENSNameValidator
import com.unifina.utils.StreamIDPathValidator
import grails.compiler.GrailsCompileStatic
import grails.validation.Validateable

@GrailsCompileStatic
@Validateable
class StreamID {
	String domain
	String path

	StreamID(String s) {
		if (s == null) {
			throw new IllegalArgumentException("ENS name cannot be null")
		}
		String[] components = s.split("/", 2)
		switch (components.length) {
			case 1:
				this.domain = components[0]
				break
			case 2:
				this.domain = components[0]
				this.path = components[1]
				break
			default:
				throw new IllegalArgumentException("ENS name has illegal format")
		}
	}

	static constraints = {
		domain(nullable: false, validator: ENSNameValidator.validate)
		path(nullable: true, validator: StreamIDPathValidator.validate)
	}

	@Override
	String toString() {
		if (this.path == null || this.path == "" ) {
			return this.domain
		}
		return this.domain + '/' + this.path
	}
}
