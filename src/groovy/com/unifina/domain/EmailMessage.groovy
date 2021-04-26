package com.unifina.domain

import grails.validation.Validateable
import groovy.transform.ToString

@Validateable
@ToString
class EmailMessage {
	String sharer
	String to
	String subjectTemplate
	Resource resource

	EmailMessage(String sharer, String to, String subjectTemplate, Resource resource) {
		this.sharer = sharer ?: "Streamr user"
		this.to = to
		this.subjectTemplate = subjectTemplate
		this.resource = resource
	}

	String resourceType() {
		if (Stream.isAssignableFrom(resource.clazz)) {
			return "stream"
		}
		throw new IllegalArgumentException("Unexpected resource class: " + resourceClass)
	}

	String resourceName() {
		def res = resource.clazz.get(resource.idToString())
		if (!res) {
			return ""
		}
		return res.name
	}

	String subject() {
		String subject = subjectTemplate.replace("%USER%", sharer)
		subject = subject.replace("%RESOURCE%", resourceType())
		return subject
	}

	String link() {
		if (Stream.isAssignableFrom(resource.clazz)) {
			return "/core/stream/show/" + resource.idToString()
		}
		throw new IllegalArgumentException("Unexpected resource class: " + resource.clazz)
	}
}
