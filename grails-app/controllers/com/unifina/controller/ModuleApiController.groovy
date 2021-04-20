package com.unifina.controller

import com.unifina.domain.Module
import com.unifina.domain.User
import com.unifina.service.ModuleService
import com.unifina.service.NotFoundException
import com.unifina.service.PermissionService
import com.unifina.signalpath.AbstractSignalPathModule
import com.unifina.signalpath.ModuleException
import com.unifina.signalpath.custom.ModuleExceptionMessage
import com.unifina.utils.Globals
import grails.compiler.GrailsCompileStatic
import grails.converters.JSON
import grails.util.GrailsUtil

class ModuleApiController {

	PermissionService permissionService
	ModuleService moduleService

	@StreamrApi
	def index() {
		List<Module> mods = Module.createCriteria().list {
			isNull("hide")
		}
		render mods as JSON
	}

	@StreamrApi(authenticationLevel = AuthLevel.NONE)
	def help(Long id) {
		findModule(id, { Module module ->
			response.setContentType("application/json")
			render(module.jsonHelp ? module.jsonHelp.replace("\n", "") : "{}")
		})
	}

	@StreamrApi
	def jsonGetModule(Long id) {
		Map moduleConfig = request.JSON ?: [:]
		User user = request.apiUser

		try {
			Map iMap = instantiateAndGetConfig(id, moduleConfig, user)
			render iMap as JSON
		} catch (Exception e) {
			List<ModuleExceptionMessage> moduleExceptions = []
			def me = e

			// Find a possible ModuleException in the cause hierarchy
			while (me != null) {
				if (me instanceof ModuleException) {
					moduleExceptions = ((ModuleException) me).getModuleExceptions()
					break
				} else {
					me = me.cause
				}
			}

			e = GrailsUtil.deepSanitize(e)
			log.error("Exception while creating module!", e)
			Map r = [
				error       : true,
				message     : e.message,
				moduleErrors: moduleExceptions*.toMap()
			]
			render r as JSON
		}
	}

	@GrailsCompileStatic
	private Map instantiateAndGetConfig(Long id, Map moduleConfig, User user) {
		Globals globals = new Globals([:], user)

		Module domainObject = Module.get(id)
		if (domainObject == null) {
			throw new NotFoundException(Module.simpleName, id.toString())
		}
		AbstractSignalPathModule m = moduleService.getModuleInstance(domainObject, moduleConfig, null, globals)
		m.connectionsReady()

		Map iMap = m.configuration

		// Augment the json map with some representation- and id-related stuff
		iMap.put("id", domainObject.id)
		iMap.put("name", m.name)
		iMap.put("jsModule", domainObject.jsModule)
		iMap.put("type", domainObject.type)

		return iMap
	}

	private void findModule(Long id, Closure action) {
		def module = Module.get(id)
		if (!module) {
			throw new NotFoundException("Module", id)
		} else {
			action.call(module)
		}
	}
}
