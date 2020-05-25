package com.unifina.domain

import com.unifina.domain.dashboard.Dashboard
import com.unifina.domain.data.Stream
import com.unifina.domain.signalpath.Canvas
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
		if (Canvas.isAssignableFrom(resource.clazz)) {
			return "canvas"
		} else if (Stream.isAssignableFrom(resource.clazz)) {
			return "stream"
		} else if (Dashboard.isAssignableFrom(resource.clazz)) {
			return "dashboard"
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
		if (Canvas.isAssignableFrom(resource.clazz)) {
			return "/canvas/editor/" + resource.idToString()
		} else if (Stream.isAssignableFrom(resource.clazz)) {
			return "/core/stream/show/" + resource.idToString()
		} else if (Dashboard.isAssignableFrom(resource.clazz)) {
			return "/dashboard/editor/" + resource.idToString()
		}
		throw new IllegalArgumentException("Unexpected resource class: " + resource.clazz)
	}
}
