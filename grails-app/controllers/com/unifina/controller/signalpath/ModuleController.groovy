package com.unifina.controller.signalpath

import grails.converters.JSON
import grails.util.GrailsUtil

import org.apache.log4j.Logger

import com.unifina.domain.signalpath.Module;
import com.unifina.domain.signalpath.ModuleCategory;
import com.unifina.domain.signalpath.ModulePackage;
import com.unifina.signalpath.AbstractSignalPathModule;
import com.unifina.signalpath.IReturnChannel;
import com.unifina.signalpath.ModuleException;
import com.unifina.utils.Globals
import com.unifina.utils.GlobalsFactory

class ModuleController {
	
	def moduleService
	def grailsApplication
	def springSecurityService
	def unifinaSecurityService
	
	private static final Logger log = Logger.getLogger(ModuleController)
	
	def jsonSearchModule() {
		Set<ModulePackage> allowedPackages = springSecurityService.currentUser?.modulePackages ?: new HashSet<>()
		List<Module> mods = []
		
		if (!allowedPackages.isEmpty()) {
			mods = Module.createCriteria().list {
				isNull("hide")
				like("name","%"+params.term+"%")
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
		def categories = ModuleCategory.findAllByParentIsNull([sort:"sortOrder"])

		Set<ModulePackage> allowedPackages = springSecurityService.currentUser?.modulePackages ?: new HashSet<>()
		allowedPackages.addAll(ModulePackage.findAllByUser(springSecurityService.currentUser))
		
		def result = []	
		categories.findAll{allowedPackages.contains(it.modulePackage)}.each {category->
			def item = moduleTreeRecurse(category,allowedPackages)
			result.add(item)
		}
		render result as JSON
	}

	private Map moduleTreeRecurse(ModuleCategory category, Set<ModulePackage> allowedPackages) {
		def item = [:]
		item.data = category.name
		item.metadata = [canAdd:false, id:category.id]
		item.children = []

		category.subcategories.findAll{allowedPackages.contains(it.modulePackage)}.each {subcat->
			def subItem = moduleTreeRecurse(subcat,allowedPackages)
			item.children.add(subItem)
		}

		category.modules.each {Module module->
			if (allowedPackages.contains(module.modulePackage) && (module.hide==null || !module.hide)) {
				def moduleItem = [:]
				moduleItem.data = module.name
				moduleItem.metadata = [canAdd:true, id:module.id]
				item.children.add(moduleItem)
			}
		}

		return item
	}

	def jsonGetModule() {
		Globals globals = GlobalsFactory.createInstance([:], grailsApplication)
		
		try {
			Module domainObject = Module.get(params.id)
			if (!unifinaSecurityService.canAccess(domainObject)) {
				throw new Exception("User $springSecurityService.currentUser does not have access to module $domainObject.name")
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

			//		 TODO: yleista
//			if (m instanceof com.unifina.signalpath.rapidminer.RapidMinerModel && params.rapidModelId) {
//				m.loadModel(Long.parseLong(params.rapidModelId))
//				iMap["model"] = [rapidModelId:params.rapidModelId]
//			}
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
//			e.printStackTrace(System.out)
			Map r = [error:true, message:e.message, moduleErrors:moduleExceptions]
			render r as JSON
		}
		finally {
			globals.destroy()
		}
	}
	
	
	/**
	 * Used to communicate back user actions in the UI
	 */
	def uiAction() {
		String sessionId = params.sessionId
		def msg = JSON.parse(params.msg)
		def hash = params.int("hash")
		
		Map r
		IReturnChannel channel = servletContext["returnChannels"]?.get(sessionId)
		if (channel) {
			channel.signalPath.getModule(hash).receiveUIMessage(msg)
			r = [success:true, sessionId:sessionId, hash:hash, msg:msg]
		}
		else r = [success:false, sessionId:sessionId, hash:hash, msg:msg, error:"Session not found"]
		
		render r as JSON
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
			render ([success:false, error: "Access denied, only owner can edit module help"])
		}
		else {
			module.jsonHelp = params.jsonHelp
			module.save(failOnError:true)
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
	
}
