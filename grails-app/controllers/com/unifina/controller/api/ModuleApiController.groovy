package com.unifina.controller.api

import com.unifina.api.NotFoundException
import com.unifina.api.NotPermittedException
import com.unifina.domain.security.Permission
import com.unifina.domain.signalpath.Module
import com.unifina.security.StreamrApi
import grails.plugin.springsecurity.annotation.Secured

@Secured(["IS_AUTHENTICATED_ANONYMOUSLY"])
class ModuleApiController {

	def permissionService

	@StreamrApi(requiresAuthentication = false)
	def help(Long id) {
		getAuthorizedModule(id, Permission.Operation.READ) {Module module ->
			response.setContentType("application/json")
			render module.jsonHelp ?: "{}"
		}
	}

	/**
	 * Access to a Module is granted if the same operation is permitted on the
	 * ModulePackage the Module belongs to
     */
	private void getAuthorizedModule(Long id, Permission.Operation op, Closure action) {
		def module = Module.get(id)
		if (!module) {
			throw new NotFoundException("Module", id)
		} else if (!permissionService.check(request.apiUser, module.modulePackage, op)) {
			throw new NotPermittedException(request.apiUser?.username, "ModulePackage", module.modulePackage.id.toString(), op.id)
		} else {
			action.call(module)
		}
	}

}
