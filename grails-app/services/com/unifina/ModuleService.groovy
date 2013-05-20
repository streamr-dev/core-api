package com.unifina

import groovy.transform.CompileStatic

import com.unifina.signalpath.AbstractSignalPathModule
import com.unifina.signalpath.ISharedInstance
import com.unifina.signalpath.Module
import com.unifina.signalpath.SignalPath
import com.unifina.utils.Globals

class ModuleService {

	@CompileStatic
    public AbstractSignalPathModule getModuleInstance(Module mod, Map config, SignalPath parent, Globals globals) {
		// Load the class using the classloader of the Globals class so that the classes loaded
		// for this SignalPath run can be unloaded when the run finishes.
		AbstractSignalPathModule m = (AbstractSignalPathModule) globals.groovyClassLoader.loadClass(mod.implementingClass).newInstance()
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
	
}
