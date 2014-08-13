package com.unifina.service

import java.util.List;
import java.util.Map;

import groovy.transform.CompileStatic

import com.unifina.domain.signalpath.Module
import com.unifina.signalpath.AbstractSignalPathModule
import com.unifina.signalpath.SignalPath
import com.unifina.utils.Globals

class ModuleService {

	@CompileStatic
    public AbstractSignalPathModule getModuleInstance(Module mod, Map config, SignalPath parent, Globals globals) {
		// TODO: check that the owning user has the privileges to access this module
		
		// Load the class using the classloader of the Globals class so that the classes loaded
		// for this SignalPath run can be unloaded when the run finishes.
		ClassLoader cl = this.getClass().getClassLoader()
		AbstractSignalPathModule m = (AbstractSignalPathModule) cl.loadClass(mod.implementingClass).newInstance()
//		AbstractSignalPathModule m = (AbstractSignalPathModule) globals.classLoader.loadClass(mod.implementingClass).newInstance()
//		AbstractSignalPathModule m = (AbstractSignalPathModule) new GroovyClassLoader().loadClass(mod.implementingClass).newInstance()
		m.globals = globals
		m.init()
		m.setName(mod.name)
		
		if (parent!=null)
			m.parentSignalPath = parent
			
		if (config!=null) {
			// Make sure the config has up-to-date info about the Module
			config["id"] = mod.id
			config["jsModule"] = mod.jsModule
			config["name"] = mod.name
			config["type"] = mod.type
			m.configure(config)
		}

//		if (m instanceof ISharedInstance && globals) {
//			AbstractSignalPathModule shared = globals.sharedInstances.get(m.getSharedInstanceID())
//			if (shared==null) {
//				globals.sharedInstances.put(m.getSharedInstanceID(),m)
//			}
//			else {
//				// Return the shared instance and don't call Globals#onModuleCreated()
//				return shared
//			}
//		}

		globals?.onModuleCreated(m)
		return m
    }
	
	public AbstractSignalPathModule getModuleInstance(String clazz, Map config, SignalPath parent, Globals globals) {
		Module m = Module.findByImplementingClass(clazz)
		return getModuleInstance(m, config, parent, globals)
	}
	
	public AbstractSignalPathModule getModuleInstance(Number id, Map config, SignalPath parent, Globals globals) {
		Module m = Module.get(id)
		return getModuleInstance(m, config, parent, globals)
	}

	public List<Module> getModuleDomainObjects(
			List<Map> moduleConfigs) {
		// Collect module ids
		List ids = moduleConfigs.collect {it.id}
		return Module.findAllByIdInList(ids)
	}
}
