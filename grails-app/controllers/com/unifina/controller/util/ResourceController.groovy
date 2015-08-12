package com.unifina.controller.util

import grails.plugin.springsecurity.annotation.Secured

@Secured("ROLE_USER")
class ResourceController {
	def index() {
		render g.resource(dir:params.dir, file:params.file, absolute:params.absolute, plugin:'unifina-core')
	}
}
