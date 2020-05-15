package com.unifina.domain

import grails.compiler.GrailsCompileStatic
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import org.codehaus.groovy.grails.commons.DomainClassArtefactHandler

@ToString
@GrailsCompileStatic
@EqualsAndHashCode
class Resource {
	Class<?> clazz
	Object id

	Resource(Class<?> clazz, Object id) {
		if (!clazz) {
			throw new IllegalArgumentException("Missing resource class")
		}
		if (!DomainClassArtefactHandler.isDomainClass(clazz)) {
			throw new IllegalArgumentException("Not a valid Grails domain class: " + clazz.simpleName)
		}
		this.clazz = clazz
		if (!id) {
			throw new IllegalArgumentException("Missing resource id")
		}
		this.id = id
	}

	String idToString() {
		return id?.toString()
	}
}
