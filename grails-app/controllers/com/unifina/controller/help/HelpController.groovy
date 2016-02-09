package com.unifina.controller.help

import grails.plugin.springsecurity.annotation.Secured

@Secured(["ROLE_USER"])
class HelpController {

	static defaultAction = "userGuide"

	def userGuide() {

	}

	def api() {

	}
}