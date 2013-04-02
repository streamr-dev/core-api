package com.unifina.signalpath

import java.util.Map;

import groovy.transform.CompileStatic


class SignalPathParameter extends Parameter<SavedSignalPath> {

	Closure criteria
	
	public SignalPathParameter(AbstractSignalPathModule owner, String name) {
		super(owner, name, null, "SignalPath");
		def proj = {
			projections {
				property 'id', 'id'
				property 'name', 'name'
			}
		}

		// Combine closures
		criteria = proj << getCriteria()
	}
	
	def getCriteria() {
		return {
			eq("hasExports",true)
		}
	}
	
	public Map<String,Object> getConfiguration() {
		Map<String,Object> config = super.getConfiguration()
		
		if (value!=null) {
			config.put("value", getValue().getId());
			config.put("defaultValue", getValue().getId());
		}

		Collection signalPaths = SavedSignalPath.createCriteria().list(criteria)
		config.put("possibleValues",signalPaths.collect {[value:it[0], name:it[1]]})
		return config
	}

	@Override
	SavedSignalPath parseValue(String s) {
		try {
			return SavedSignalPath.get(Long.parseLong(s));
		} catch (NumberFormatException e) {
			return null
		}
	}
	
}
