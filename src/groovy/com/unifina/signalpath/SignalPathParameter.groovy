package com.unifina.signalpath

import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails

import com.unifina.domain.signalpath.SavedSignalPath;


class SignalPathParameter extends Parameter<SavedSignalPath> {
	
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
