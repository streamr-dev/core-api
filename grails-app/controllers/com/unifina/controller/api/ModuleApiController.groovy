package com.unifina.controller.api

import com.unifina.api.NotFoundException
import com.unifina.api.NotPermittedException
import com.unifina.domain.data.Stream
import com.unifina.domain.security.Permission
import com.unifina.domain.signalpath.Module
import com.unifina.domain.signalpath.ModulePackage
import com.unifina.security.AuthLevel
import com.unifina.security.StreamrApi
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured

@Secured(["IS_AUTHENTICATED_ANONYMOUSLY"])
class ModuleApiController {

	def permissionService

	@StreamrApi
	def index() {
		Set<ModulePackage> allowedPackages = permissionService.getAll(ModulePackage, request.apiUser) ?: new HashSet<>()
		List<Module> mods = []

		if (!allowedPackages.isEmpty()) {
			mods = Module.createCriteria().list {
				isNull("hide")
				or {
					'in'("modulePackage", allowedPackages)
					modulePackage {
						eq("user", request.apiUser)
					}
				}
			}
		}

		render mods as JSON
	}

	@StreamrApi(authenticationLevel = AuthLevel.NONE)
	def help(Long id) {
		getAuthorizedModule(id, Permission.Operation.READ) {Module module ->
			response.setContentType("application/json")
			render (module.jsonHelp ? module.jsonHelp.replace("\n", "") : "{}")
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
