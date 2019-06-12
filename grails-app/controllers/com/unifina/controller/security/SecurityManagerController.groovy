package com.unifina.controller.security

import com.unifina.security.ProfilingSecurityManager;

import grails.plugin.springsecurity.annotation.Secured

@Secured(["ROLE_ADMIN"])
class SecurityManagerController {
	def index() {
		SecurityManager sm = System.securityManager
		if (sm instanceof ProfilingSecurityManager) {
			List rules = ((ProfilingSecurityManager)sm).getRules()
			render rules.join("<br>")
		}
		else render "SecurityManager is of type: ${sm?.getClass()}"
	}
}
