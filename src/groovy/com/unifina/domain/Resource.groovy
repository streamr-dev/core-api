package com.unifina.domain

import grails.compiler.GrailsCompileStatic
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

@ToString
@GrailsCompileStatic
@EqualsAndHashCode
class Resource {
	Class<?> clazz
	Object id

	Resource(Class<?> clazz, Object id) {
		// TODO: if (!clazz) { throw new IllegalArgumentException("Missing resource class") }
		this.clazz = clazz
		this.id = id
	}
	String idToString() {
		return this?.id?.toString()
	}
}
