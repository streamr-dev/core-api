package com.unifina.signalpath

import com.unifina.utils.Globals
import com.unifina.utils.GlobalsFactory;

import grails.converters.JSON
import grails.util.GrailsUtil

class ModuleController {
	
	def moduleService
	def grailsApplication
	
	def jsonSearchModule() {
		List<Module> mods = Module.findAllByHideIsNullAndNameLike("%"+params.term+"%")
		render mods as JSON
	} 
	
	def jsonGetModules() {
		List<Module> mods = Module.findAllByHideIsNull()
		render mods as JSON
	}
	
	def jsonGetModuleTree() {
		def categories = ModuleCategory.findAllByParentIsNull([sort:"sortOrder"])
		def result = []
		categories.each {category->
			def item = moduleTreeRecurse(category)
			result.add(item)
		}
		render result as JSON
	}

	def moduleTreeRecurse(ModuleCategory category) {
		def item = [:]
		item.data = category.name
		item.metadata = [canAdd:false, id:category.id]
		item.children = []

		category.subcategories.each {subcat->
			def subItem = moduleTreeRecurse(subcat)
			item.children.add(subItem)
		}

		category.modules.each {module->
			if (module.hide==null || !module.hide) {
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
//		globals.target.createModule = true
		
		try {
			def domainObject = Module.get(params.id)
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
			e = GrailsUtil.deepSanitize(e)
			e.printStackTrace(System.out)
			Map r = [error:true, message:e.message]
			render r as JSON
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
}
