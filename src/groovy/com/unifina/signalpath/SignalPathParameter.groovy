package com.unifina.signalpath

import com.unifina.domain.signalpath.Canvas

class SignalPathParameter extends Parameter<Canvas> {
	
	public SignalPathParameter(AbstractSignalPathModule owner, String name) {
		super(owner, name, null, "Canvas");
	}
	
	public Map<String,Object> getConfiguration() {
		Map<String,Object> config = super.getConfiguration()
		
		if (getValue() != null) {
			config.put("value", getValue().getId());
			config.put("defaultValue", getValue().getId());
		}

		def permissionService = owner.globals.grailsApplication.mainContext.getBean("permissionService")
		def user = owner.globals.getUser()
		Collection signalPaths = permissionService.get(Canvas, user) {
			projections {
				property 'id', 'id'
				property 'name', 'name'
			}
			eq("hasExports", true)
			eq("adhoc", false)
			order("lastUpdated", "desc")
		}

		List possibleValues = signalPaths.collect {[value:it[0], name:it[1]]}
		
		if (getValue() == null)
			possibleValues.add(0, [value:null, name: "Select >>"])
			
		config.put("possibleValues", possibleValues)

		return config
	}

	@Override
	Canvas parseValue(String s) {
		try {
			return Canvas.get(s);
		} catch (NumberFormatException e) {
			return null
		}
	}
	
}
