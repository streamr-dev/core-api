package com.unifina.controller.signalpath

import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import grails.util.GrailsUtil
import groovy.json.JsonSlurper

import org.apache.log4j.Logger

import com.unifina.domain.signalpath.Module
import com.unifina.domain.signalpath.ModuleCategory
import com.unifina.domain.signalpath.ModulePackage
import com.unifina.signalpath.AbstractSignalPathModule
import com.unifina.signalpath.ModuleException
import com.unifina.utils.Globals
import com.unifina.utils.GlobalsFactory

@Secured(["ROLE_USER"])
class ModuleController {
	
	def moduleService
	def grailsApplication
	def springSecurityService
	def unifinaSecurityService
	
	static defaultAction = "list"
	
	private static final Logger log = Logger.getLogger(ModuleController)
	
	def list() {
		
	}
	
	def jsonSearchModule() {
		Set<ModulePackage> allowedPackages = springSecurityService.currentUser?.modulePackages ?: new HashSet<>()
		List<Module> mods = []
		
		if (!allowedPackages.isEmpty()) {
			mods = Module.createCriteria().list {
				isNull("hide")
				or {
					like("name","%"+params.term+"%")
					like("alternativeNames","%"+params.term+"%")
				}
				or {
					'in'("modulePackage",allowedPackages)
					modulePackage {
						eq("user",springSecurityService.currentUser)
					}
				}
			}
			mods = mods.sort({Module x ->
				if(x.name.toLowerCase().equals(params.term.toLowerCase()))
					return 0
				else if(x.name.toLowerCase().startsWith(params.term.toLowerCase()))
					return 1
				else
					return 2
			})
		}

		render mods as JSON
	} 
	
	def jsonGetModules() {
		Set<ModulePackage> allowedPackages = springSecurityService.currentUser?.modulePackages ?: new HashSet<>()
		List<Module> mods = []
		
		if (!allowedPackages.isEmpty()) {
			mods = Module.createCriteria().list {
				isNull("hide")
				or {
					'in'("modulePackage",allowedPackages)
					modulePackage {
						eq("user",springSecurityService.currentUser)
					}
				}
			}
		}

		render mods as JSON
	}
	
	def jsonGetModuleTree() {
		def categories = ModuleCategory.findAllByParentIsNullAndHideIsNull([sort:"sortOrder"])

		Set<ModulePackage> allowedPackages = springSecurityService.currentUser?.modulePackages ?: new HashSet<>()
		allowedPackages.addAll(ModulePackage.findAllByUser(springSecurityService.currentUser))
		
		Set<Long> allowedPackageIds = allowedPackages.collect {it.id} as Set
		
		def result = []	
		categories.findAll{allowedPackageIds.contains(it.modulePackage.id)}.each {category->
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

	def jsonGetModule() {
		Globals globals = GlobalsFactory.createInstance([:], grailsApplication)
		
		try {
			Module domainObject = Module.get(params.id)
			if (!unifinaSecurityService.canAccess(domainObject)) {
				throw new Exception("Access denied for user $springSecurityService.currentUser.username to requested module")
			}
			
			def conf = (params.configuration ? JSON.parse(params.configuration) : [:])

			AbstractSignalPathModule m = moduleService.getModuleInstance(domainObject,conf,null,globals)
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
		finally {
			globals.destroy()
		}
	}
	
	def jsonGetModuleHelp() {
		Module module = Module.get(params.id)
		if (!unifinaSecurityService.canAccess(module)) {
			throw new Exception("User $springSecurityService.currentUser does not have access to module $module.name")
		}
		else {
			response.setContentType("application/json")
			render module.jsonHelp ?: "{}"
		}
	}
	
	def jsonSetModuleHelp() {
		// Needs to be owner
		Module module = Module.get(params.id)
		if (module.modulePackage.user!=springSecurityService.currentUser) {
			response.status = 403
			render ([success:false, error: "Access denied, only owner can edit module help"] as JSON)
		} else {
			module.jsonHelp = params.jsonHelp
			module.save(failOnError:true, flush:true)
			render ([success:true] as JSON)
		}
	}
	
	def editHelp() {
		// Needs to be owner
		Module module = Module.get(params.long("id"))
		if (module.modulePackage.user!=springSecurityService.currentUser) {
			throw new Exception("User $springSecurityService.currentUser can not edit help of module $module.name")
		}
		[module:module]
	}
	
	def canEdit() {
		Module module = Module.get(params.id)
		if (module.modulePackage.user!=springSecurityService.currentUser) {
			render ([success:false] as JSON)
		} else {
			render ([success:true] as JSON)
		}
	}
	
}
