package com.unifina.controller.api

import com.unifina.api.NotFoundException
import com.unifina.api.NotPermittedException
import com.unifina.domain.data.Stream
import com.unifina.domain.security.Permission
import com.unifina.domain.security.SecUser
import com.unifina.domain.signalpath.Module
import com.unifina.domain.signalpath.ModuleCategory
import com.unifina.domain.signalpath.ModulePackage
import com.unifina.security.AuthLevel
import com.unifina.security.StreamrApi
import com.unifina.signalpath.AbstractSignalPathModule
import com.unifina.signalpath.ModuleException
import com.unifina.utils.Globals
import com.unifina.utils.GlobalsFactory
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import grails.util.GrailsUtil

@Secured(["IS_AUTHENTICATED_ANONYMOUSLY"])
class ModuleApiController {

	def permissionService
	def moduleService

	@StreamrApi
	def index() {
		Set<ModulePackage> allowedPackages = permissionService.getAll(ModulePackage, request.apiUser) ?: new HashSet<>()
		List<Module> mods = []

		if (!allowedPackages.isEmpty()) {
			mods = Module.createCriteria().list {
				isNull("hide")
				'in'("modulePackage", allowedPackages)
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

	@StreamrApi
	def jsonGetModule() {
		SecUser user = request.apiUser
		Globals globals = GlobalsFactory.createInstance([:], user)

		try {
			Module domainObject = Module.get(params.long("id"))
			if (!permissionService.canRead(user, domainObject.modulePackage)) {
				throw new Exception("Access denied for user $user.username to requested module")
			}

			def conf = (params.configuration ? JSON.parse(params.configuration) : [:])

			AbstractSignalPathModule m = moduleService.getModuleInstance(domainObject, conf, null, globals)
			m.connectionsReady()

			Map iMap = m.configuration

			// Augment the json map with some representation- and id-related stuff
			iMap.put("id", domainObject.id)
			iMap.put("name", m.name)
			iMap.put("jsModule", domainObject.jsModule)
			iMap.put("type", domainObject.type)

			render iMap as JSON
		} catch (Exception e) {
			def moduleExceptions = []
			def me = e

			// Find a possible ModuleException in the cause hierarchy
			while (me!=null) {
				if (me instanceof ModuleException) {
					moduleExceptions = ((ModuleException)me).getModuleExceptions().collect {
						[hash:it.hash, payload:it.msg]
					}
					break
				}
				else me = me.cause
			}

			e = GrailsUtil.deepSanitize(e)
			log.error("Exception while creating module!",e)
			Map r = [error:true, message:e.message, moduleErrors:moduleExceptions]
			render r as JSON
		}
	}

	@StreamrApi
	def jsonGetModuleTree() {
		def categories = ModuleCategory.findAllByParentIsNullAndHideIsNull([sort:"sortOrder"])

		Set<ModulePackage> allowedPackages = permissionService.getAll(ModulePackage, request.apiUser) ?: new HashSet<>()

		Set<Long> allowedPackageIds = allowedPackages.collect {it.id} as Set

		def result = []
		categories.findAll {
			allowedPackageIds.contains(it.modulePackage.id)
		}.each {category->
			def item = moduleTreeRecurse(category,allowedPackageIds,params.boolean('modulesFirst') ?: false)
			result.add(item)
		}
		render result as JSON
	}

	private Map moduleTreeRecurse(ModuleCategory category, Set<Long> allowedPackageIds, boolean modulesFirst=false) {
		def item = [:]
		item.data = category.name
		item.metadata = [canAdd:false, id:category.id]
		item.children = []

		List categoryChildren = []
		List moduleChildren = []

		category.subcategories.findAll{allowedPackageIds.contains(it.modulePackage.id)}.each {subcat->
			def subItem = moduleTreeRecurse(subcat,allowedPackageIds, modulesFirst)
			categoryChildren.add(subItem)
		}

		category.modules.each {Module module->
			if (allowedPackageIds.contains(module.modulePackage.id) && (module.hide==null || !module.hide)) {
				def moduleItem = [:]
				moduleItem.data = module.name
				moduleItem.metadata = [canAdd:true, id:module.id]
				moduleChildren.add(moduleItem)
			}
		}

		if (modulesFirst) {
			item.children.addAll(moduleChildren)
			item.children.addAll(categoryChildren)
		}
		else {
			item.children.addAll(categoryChildren)
			item.children.addAll(moduleChildren)
		}

		return item
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
