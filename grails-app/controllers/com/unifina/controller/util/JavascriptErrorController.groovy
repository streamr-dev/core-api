package com.unifina.controller.util

import grails.plugin.springsecurity.annotation.Secured

import org.apache.log4j.Logger

@Secured(["ROLE_USER"])
class JavascriptErrorController {
	
	private static final Logger log = Logger.getLogger(JavascriptErrorController)
	
	def logError() {
		log.error("JavaScript error at url $params.url: $params.errorMsg, line $params.line, column $params.column, stack: $params.stack")
		render ""
	}
}
