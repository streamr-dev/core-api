package com.unifina.service

import org.codehaus.groovy.grails.commons.GrailsApplication

import javax.management.ObjectName
import java.lang.management.ManagementFactory

class MetricsService {
	GrailsApplication grailsApplication

	Object numOfSessionsTomcat() {
		ObjectName name = new ObjectName(grailsApplication.config.streamr.metrics.numberOfSessions)
		(Integer) ManagementFactory.getPlatformMBeanServer().getAttribute(name, "activeSessions")
	}
}
