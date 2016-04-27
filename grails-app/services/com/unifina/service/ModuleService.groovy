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

		ClassLoader cl = this.getClass().getClassLoader()
		AbstractSignalPathModule m = (AbstractSignalPathModule) cl.loadClass(mod.implementingClass).newInstance()
		m.globals = globals
		m.init()
		m.setName(mod.name)
		m.setDomainObject(mod);
		if (parent != null) { m.parentSignalPath = parent }

		// Make sure the config contains up-to-date info about the Module
		if (config != null) {
			config["id"] = mod.id
			config["jsModule"] = mod.jsModule
			config["name"] = mod.name
			config["type"] = mod.type
			m.configure(config)
		}

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

	public List<Module> getModuleDomainObjects(List<Map> moduleConfigs) {
		if (moduleConfigs == null || moduleConfigs.isEmpty()) {
			return []
		} else {
			List ids = moduleConfigs.collect { (long)it.id }
			return Module.findAllByIdInList(ids)
		}
	}
}
