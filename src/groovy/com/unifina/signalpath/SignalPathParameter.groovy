package com.unifina.signalpath

import com.unifina.domain.signalpath.Canvas

class SignalPathParameter extends Parameter<Canvas> {
	
	public SignalPathParameter(AbstractSignalPathModule owner, String name) {
		super(owner, name, null, "Canvas");
	}
	
	def getCriteria() {
		def springSecurityService = owner.globals?.grailsApplication?.mainContext?.getBean("springSecurityService")
		def user = springSecurityService?.currentUser
		
		if (user) {
			return {
				eq("hasExports",true)
				eq("user",user)
			}
		}
		else return null
	}
	
	public Map<String,Object> getConfiguration() {
		Map<String,Object> config = super.getConfiguration()
		
		if (value!=null) {
			config.put("value", getValue().getId());
			config.put("defaultValue", getValue().getId());
		}

		Collection signalPaths
		def crit = getCriteria()
		if (crit!=null) {
			def proj = {
				projections {
					property 'id', 'id'
					property 'name', 'name'
				}
			}
			signalPaths = SavedSignalPath.createCriteria().list(proj << crit)
		}
		else signalPaths = []

		List possibleValues = signalPaths.collect {[value:it[0], name:it[1]]}
		
		if (value==null)
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
